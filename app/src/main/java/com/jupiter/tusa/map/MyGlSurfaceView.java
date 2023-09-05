package com.jupiter.tusa.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.map.scale.MapScaleListener;
import com.jupiter.tusa.utils.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyGlSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private MainActivity mainActivity;
    private int executorPoolsAmount = 2;
    private ExecutorService executorService = Executors.newFixedThreadPool(executorPoolsAmount);

    // Маштабирование карты
    private GestureDetector gestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;

    // тест
    boolean disableRenderMap = false;

    // Отрисовка карты
    final int tilesAmount = 80;
    private int mapZ = 0;
    private int maxMapZ = 18;
    private float maxBottomRightZoneWorldLength = (float) Math.pow(2, maxMapZ);
    private float useTileSize = determineTileSize();

    private Tile[] renderedTiles = new Tile[tilesAmount];

    // animations
    private List<Tile> fadeOutTiles = new ArrayList<Tile>();

    // Перемещение карты
    private float previousX = 0f;
    private float previousY = 0f;
    private int currentTileX = 0;
    private int currentTileY = 0;
    private int previousCurrentTileX = currentTileX;
    private int previousCurrentTileY = currentTileY;
    private float currentMapX = 0;
    private float currentMapY = 0;
    private float moveStrong = 100f;
    private float maxScaleFactor = useTileSize;
    private CancellationMapAction previousUpdateMapZ = new CancellationMapAction();
    private Future<?> renderMapFuture;

    public float getCurrentMapX() {
        return currentMapX;
    }

    public float getCurrentMapY() {
        return currentMapY;
    }

    public float getScaleFactor() {
        return mapScaleListener.getScaleFactor();
    }

    // Применение тайлов после загрузки
    private OnPrepareSprite onSpriteReady = new OnPrepareSprite() {
        @Override
        public void ready(Bitmap bitmap, float[] vertexLocations, int useIndex, RenderTileInitiator initiator, Tile tile) {
            if(tile.isCancelled())
                return;

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if(initiator == RenderTileInitiator.ZOOM_DOWN) {
                        renderTile(tile);
                    }
                    if(initiator == RenderTileInitiator.CHANGE_SURFACE) {
                        renderTile(tile);
                    } else if(initiator == RenderTileInitiator.MOVE) {
                        renderTile(tile);
                    } else if(initiator == RenderTileInitiator.ZOOM_UP) {
                        renderTile(tile);
                    }
                }
            });
            requestRender();
        }
    };

    private void clearTile(int index) {
        Tile tile = renderedTiles[index];
        if(tile != null) {
            tile.cancelLoadTile();
            renderedTiles[index] = null;
        }
        renderer.setNullPointerForSprite(index);
    }

    private void renderTile(Tile tile) {
        Sprite sprite = new Sprite(
                mainActivity,
                tile.getBitmap(),
                tile.getUseIndex(),
                tile.getVertexLocations()
        );
        tile.setSprite(sprite);
        fadeOutTiles.add(tile);
        renderer.renderSprite(sprite);
    }

    public MyGlSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mainActivity = (MainActivity) context;
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context, this);
        setRenderer(renderer);

        mapScaleListener = new MapScaleListener(maxScaleFactor, 1, maxScaleFactor);
        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                return true;
            }
        });

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Анимации
        Handler animationHandler = new Handler();
        long delayMillis = 60;
        animationHandler.post(new Runnable() {
            @Override
            public void run() {
                // Sprites fade out animation
                Iterator<Tile> iterator = fadeOutTiles.iterator();
                while(iterator.hasNext()) {
                    Tile fadeOutTile = iterator.next();
                    Sprite sprite = fadeOutTile.getSprite();
                    if(sprite == null)
                        continue;
                    float currentAlpha = sprite.getAlpha();
                    if(currentAlpha >= 1) {
                        iterator.remove();
                        continue;
                    }
                    float deltaAlpha = (float) delayMillis / 1000;
                    sprite.setAlpha(currentAlpha + deltaAlpha);
                }

                requestRender();
                animationHandler.postDelayed(this, delayMillis);
            }
        });

    }

    public void renderMap(int[][][] viewTiles, RenderTileInitiator initiator) {
        if(disableRenderMap)
            return;

        Log.d("GL_ARTEM", "Render map");

        // Отрендрить тайлы которые должны быть видимы
        int rows = viewTiles.length;
        int cols = viewTiles[0].length;

        int centerRow = rows / 2;
        int centerCol = cols / 2;

        // рендрим от центра к краям. Чтобы пользователь начинал видеть с центра
        for (int distance = 0; distance <= Math.max(rows, cols) / 2; distance++) {
            for (int row = centerRow - distance; row <= centerRow + distance; row++) {
                for (int col = centerCol - distance; col <= centerCol + distance; col++) {

                    // Проверяем, что индексы находятся в пределах массива
                    if (row >= 0 && row < rows && col >= 0 && col < cols) {
                        int freeIndex = -1;
                        int[] tileCoordinates = viewTiles[row][col];
                        int tileX = tileCoordinates[0];
                        int tileY = tileCoordinates[1];
                        int tileZ = tileCoordinates[2];

                        // ищем этот тайл в отрендренных
                        boolean exist = false;
                        for(int i = 0; i < renderedTiles.length; i++) {
                            Tile tile = renderedTiles[i];
                            if(tile == null) {
                                freeIndex = i;
                                continue;
                            }

                            if(tile.getX() == tileX && tile.getY() == tileY && tile.getZ() == tileZ) {
                                exist = true;
                                break;
                            }
                        }

                        // тайл уже на карте и уже существует
                        // переходим к следующему видимому тайлу
                        if(exist)
                            continue;

                        if(freeIndex == -1) {
                            Log.w("GL_ARTEM", "Map was optimized in render map! It is bad :/");
                            continue;
                        }

                        Tile tile = new Tile(mainActivity, onSpriteReady, executorService, renderer, tileX, tileY,
                                mapZ, useTileSize, freeIndex, initiator, renderedTiles, viewTiles);
                        tile.render();
                    }
                }
            }
        }
    }

    public float determineTileSize() {
        return (float) Math.pow(2, maxMapZ - mapZ);
    }

    public void optimizeTilesForStaticZ(int[][][] viewTiles) {
        // Выгрузить тайлы которые вне зоны видимости
        for(int i = 0; i < renderedTiles.length; i++) {
            Tile renderedTile = renderedTiles[i];
            if(renderedTile == null)
                continue;
            int[] leftTop = viewTiles[0][0];
            int[] rightBottom = viewTiles[viewTiles.length - 1][viewTiles[0].length - 1];
            boolean xIsNotInBounds = renderedTile.getX() > rightBottom[0] || renderedTile.getX() < leftTop[0];
            boolean yIsNotInBounds = renderedTile.getY() > rightBottom[1] || renderedTile.getY() < leftTop[1];
            boolean zIsNotInBounds = renderedTile.getZ() != mapZ;
            if(xIsNotInBounds || yIsNotInBounds || zIsNotInBounds) {
                clearTile(i);
            }
        }
    }

    // тут определяется видимая область
    // отдается массив где первый элемент(первая размерность трехмерного массива) x(ширина) второй y(высота),
    // а в третьем храняться координаты тайла x y
    // используются координаты тайлан а третьем элементе
    public int[][][] calcViewTiles() {
        // Текущая видимая область в мировых координатах
        float[] leftTopWorld = renderer.screenCoordinatesToWorldLocation(0f, 0f);
        float[] bottomRightWorld = renderer.screenCoordinatesToWorldLocation(renderer.getWidth(), renderer.getHeight());

        int maxTilesCountXOrY = (int) Math.pow(2, mapZ);
        // -1 так как без этого получается максимальное количество тайлов
        // нам же нужна максимальная координата тайла Координаты тайлов начинаются с нуля
        int maxTileXOrY = maxTilesCountXOrY - 1;

        // Технически видим область больше чтобы она успевала прогружаться
        // когда скролим вверх или влево не отрабатвыет
        // так будет меньше видимых загрузок тайлов, артефактов.
        float leftX = leftTopWorld[0];
        float topY = leftTopWorld[1];
        if(leftX < 0) {
            // рендрим только от нуля по x и y
            leftX = 0;
        }
        if(topY > 0) {
            topY = 0;
        } else {
            // перевернуть ось чтобы она соответствовала оси y тайлов
            topY = -1 * topY;
        }

        float rightX = bottomRightWorld[0];
        float bottomY = bottomRightWorld[1];

        if(rightX < 0) {
            // рендрим только от нуля по x и y
            rightX = 0;
        }

        if(bottomY > 0) {
            bottomY = 0;
        } else {
            // перевернуть ось чтобы она соответствовала оси y тайлов
            bottomY = -1 * bottomY;
        }

        // ограничиваем зону видимости размером карты
        if(bottomY >= maxBottomRightZoneWorldLength) {
            // -1 так как если не вычитать тогда будет считаться что нужно рендрить следующий тайл
            // которого нету
            bottomY = maxBottomRightZoneWorldLength - 1;
        }

        if(rightX >= maxBottomRightZoneWorldLength) {
            // -1 так как если не вычитать тогда будет считаться что нужно рендрить следующий тайл
            // которого нету
            rightX = maxBottomRightZoneWorldLength - 1;
        }

        // получаем видимость области в тайловых координатах
        float leftXViewTile = leftX / useTileSize;
        float rightXViewTile = rightX / useTileSize;

        int startX = (int) Math.floor(Math.min(leftXViewTile, rightXViewTile));
        int endX = (int) Math.floor(Math.max(leftXViewTile, rightXViewTile));

        // Увеличивыем видимую область чтобы прогружалось заранее
        if(startX > 0) {
            startX -= 1;
        }
        if(endX < maxTileXOrY) {
            endX += 1;
        }

        int[] xArray = new int[endX - startX + 1];
        for(int i = startX; i <= endX; i++) {
            xArray[i - startX] = i;
        }

        // получаем видимость области в тайловых координатах
        double bottomYViewTile = bottomY / useTileSize;
        double topYViewTile = topY / useTileSize;

        int startY = (int) Math.floor(Math.min(bottomYViewTile, topYViewTile));
        int endY = (int) Math.floor(Math.max(bottomYViewTile, topYViewTile));

        // Увеличивыем видимую область чтобы прогружалось заранее
        if(startY > 0) {
            startY -= 1;
        }
        if(endY < maxTileXOrY) {
            endY += 1;
        }

        int[] yArray = new int[endY - startY + 1];
        for(int i = startY; i <= endY; i++) {
            yArray[i - startY] = i;
        }

        //Log.d("GL_ARTEM", String.format("x %d - %d, y %d - %d", startX, endX, startY, endY));
        int[][][] viewTiles = new int[xArray.length][yArray.length][2];
        for(int xIndex = 0; xIndex < xArray.length; xIndex++) {
           for(int yIndex = 0; yIndex < yArray.length; yIndex++) {
               viewTiles[xIndex][yIndex] = new int[] {xArray[xIndex], yArray[yIndex], mapZ};
           }
        }
        return viewTiles;
    }

    public void renderMap(RenderTileInitiator renderTileInitiator) {
        if(renderMapFuture != null) {
            renderMapFuture.cancel(false);
        }
        RenderMapRunnable renderMapRunnable = new RenderMapRunnable(this, renderTileInitiator);
        renderMapFuture = executorService.submit(renderMapRunnable);
    }

    public void updateMapZ(double scaleFactor) {
        int determinedMapZ = 0;
        float determineMoveStrong = 60;

//        if(scaleFactor <= 1.5) {
//            determineMoveStrong = 0.0008f;
//            determinedMapZ = 18;
//        } else if(scaleFactor <= 4) {
//            determineMoveStrong = 0.003f;
//            determinedMapZ = 17;
//        } else if(scaleFactor <= 7) {
//            determineMoveStrong = 0.005f;
//            determinedMapZ = 16;
//        } else if(scaleFactor <= 12) {
//            determineMoveStrong = 0.009f;
//            determinedMapZ = 15;
//        } else if(scaleFactor <= 28) {
//            determineMoveStrong = 0.015f;
//            determinedMapZ = 14;
//        } else if(scaleFactor <= 55) {
//            determineMoveStrong = 0.04f;
//            determinedMapZ = 13;
//        } else if(scaleFactor <= 110) {
//            determineMoveStrong = 0.09f;
//            determinedMapZ = 12;
//        } else if(scaleFactor <= 280) {
//            determineMoveStrong = 0.2f;
//            determinedMapZ = 11;
//        } else if(scaleFactor <= 500) {
//            determineMoveStrong = 0.5f;
//            determinedMapZ = 10;
//        } else if (scaleFactor <= 1000) {
//            determineMoveStrong = 1;
//            determinedMapZ = 9;
//        } else if (scaleFactor <= 2000) {
//            determineMoveStrong = 2;
//            determinedMapZ = 8;
//        } else if(scaleFactor <= 4000) {
//            determineMoveStrong = 3;
//            determinedMapZ = 7;
//        } else if(scaleFactor <= 6000) {
//            determineMoveStrong = 6;
//            determinedMapZ = 6;
//        } else if(scaleFactor <= 17000) {
//            determineMoveStrong = 8;
//            determinedMapZ = 5;
//        } else if(scaleFactor <= 30000) {
//            determineMoveStrong = 21;
//            determinedMapZ = 4;
//        } else if(scaleFactor <= 80000) {
//            determineMoveStrong = 36;
//            determinedMapZ = 3;
//        } else if(scaleFactor <= 130000) {
//            determineMoveStrong = 50;
//            determinedMapZ = 2;
//        } else if(scaleFactor <= 190000) {
//            determineMoveStrong = 55;
//            determinedMapZ = 1;
//        }

        // логарифмическая регрессия
        determinedMapZ = (int)(18.86230239 - 1.44971501 * Math.log(scaleFactor));
        // степенная регрессия
        determineMoveStrong = (float)(0.00089951 * Math.pow(scaleFactor, 0.94762810));

        //Log.d("GL_ARTEM", "Scale factor = " + scaleFactor + " mapZ = " + determinedMapZ);

        if(determineMoveStrong != moveStrong) {
            moveStrong = determineMoveStrong;
        }

        if(determinedMapZ != mapZ) {
            boolean up = determinedMapZ > mapZ;
            RenderTileInitiator initiator = up ? RenderTileInitiator.ZOOM_UP : RenderTileInitiator.ZOOM_DOWN;
            // убираем все тайлы
            for(int i = 0; i < renderedTiles.length; i++) {
                Tile tile = renderedTiles[i];
                if(tile != null) {
                    clearTile(i);
                }
            }

            mapZ = determinedMapZ;
            useTileSize = determineTileSize();
            Log.d("GL_ARTEM", "New zoom " + mapZ);

            renderMap(initiator);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        if(event.getPointerCount() == 2) {
            // Маштабирование карты
            double scaleFactor = mapScaleListener.getScaleFactor();
            renderer.setSeeMultiply(scaleFactor);
            updateMapZ(scaleFactor);
            calcCurrentTilesXAndY();
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            // Перемещение по карте
            float dx = previousX - x;
            float dy = previousY - y;

            dx *= moveStrong;
            dy *= moveStrong;

            double realMove = MathUtils.calculateVectorLength(dx, dy);
            double realMoveLimit = moveStrong * 300;
            //Log.d("GL_ARTEM", String.format("Real move = %.3f moveStrong = %.3f limit = %.3f", realMove, moveStrong, realMoveLimit));
            if(realMove < realMoveLimit) {
                currentMapX += dx;
                currentMapY -= dy;

                calcCurrentTilesXAndY();
                //Log.d("GL_ARTEM", String.format("CurrentTileX = %d CurrentTileY = %d", currentTileX, currentTileY));
                if(currentTileY != previousCurrentTileY || currentTileX != previousCurrentTileX) {
                   renderMap(RenderTileInitiator.MOVE);
                }

                renderer.moveCameraHorizontally(currentMapX, currentMapY);
            }
        }

        previousCurrentTileX = currentTileX;
        previousCurrentTileY = currentTileY;

        previousX = x;
        previousY = y;

        requestRender();
        return true;
    }

    private void calcCurrentTilesXAndY() {
        currentTileX = (int)(currentMapX / useTileSize);
        currentTileY = (int)Math.abs(currentMapY / useTileSize);
    }
}
