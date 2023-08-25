package com.jupiter.tusa.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.map.scale.MapScaleListener;
import com.jupiter.tusa.utils.MathUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyGlSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private MainActivity mainActivity;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    // Применение тайлов после загрузки
    private OnPrepareSprite onSpriteReady = new OnPrepareSprite() {
        @Override
        public void ready(Bitmap bitmap, float[] vertexLocations, int useIndex, Tile tile) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    Sprite sprite = new Sprite(mainActivity, bitmap, useIndex, vertexLocations);
                    renderer.renderSprite(sprite, useIndex);
                }
            });
            requestRender();
        }
    };

    // Маштабирование карты
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;

    // Отрисовка карты
    final int tilesAmount = 64;
    private int mapZ = 0;
    private int maxMapZ = 18;
    private float maxBottomRightZoneWorldLength = (float) Math.pow(2, maxMapZ);
    private float useTileSize = determineTileSize();

    private Tile[] renderedTiles = new Tile[tilesAmount];

    // Перемещение карты
    private float previousX = 0f;
    private float previousY = 0f;
    private float currentMapX = 0;
    private float currentMapY = 0;
    private float moveStrong = 100f;
    private float maxScaleFactor = useTileSize;
    private float changeMapZDelta = maxScaleFactor / maxMapZ;

    public float getCurrentMapX() {
        return currentMapX;
    }

    public float getCurrentMapY() {
        return currentMapY;
    }

    public float getScaleFactor() {
        return mapScaleListener.getScaleFactor();
    }

    public MyGlSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mainActivity = (MainActivity) context;
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context, this);
        setRenderer(renderer);

        mapScaleListener = new MapScaleListener(maxScaleFactor, 1, maxScaleFactor);
        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void renderMap(int[][][] viewTiles) {
        //Log.d("GL_ARTEM", "Render map");
        // Отрендрить тайлы которые должны быть видимы
        for(int xIndex = 0; xIndex < viewTiles.length; xIndex++) {
            for(int yIndex = 0; yIndex < viewTiles[xIndex].length; yIndex++) {
                int[] tileCoordinates = viewTiles[xIndex][yIndex];
                int tileX = tileCoordinates[0];
                int tileY = tileCoordinates[1];
                int tileZ = tileCoordinates[2];

                int freeIndex = -1;

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

                if(freeIndex == -1) {
                    freeIndex = optimizeTiles(viewTiles);
                }

                // тайл уже на карте и уже существует
                // переходим к следующему видимому тайлу
                if(exist)
                    continue;

                Tile tile = new Tile(mainActivity, onSpriteReady, executorService, renderer, tileX, tileY, mapZ, useTileSize, freeIndex);
                renderedTiles[freeIndex] = tile;
                tile.render();
            }
        }
    }

    public float determineTileSize() {
        return (float) Math.pow(2, maxMapZ - mapZ);
    }

    public int optimizeTiles(int[][][] viewTiles) {
        int freeSpace = -1;
        // Выгрузить тайлы которые вне зоны видимости
        for(int i = 0; i < renderedTiles.length; i++) {
            Tile renderedTile = renderedTiles[i];
            if(renderedTile == null)
                continue;
            int[] leftTop = viewTiles[0][0];
            int[] rightBottom = viewTiles[viewTiles.length-1][viewTiles[0].length-1];
            boolean xIsNotInBounds = renderedTile.getX() > rightBottom[0] || renderedTile.getX() < leftTop[0];
            boolean yIsNotInBounds = renderedTile.getY() > rightBottom[1] || renderedTile.getY() < leftTop[1];
            if(xIsNotInBounds || yIsNotInBounds) {
                renderer.setNullPointerForSprite(i);
                renderedTiles[i] = null;
                freeSpace = i;
            }

        }

        int freeSpaceCount = 0;
        for(int i = 0; i < renderedTiles.length; i++) {
            if(renderedTiles[i] == null) {
                freeSpaceCount++;
            }
        }

        Log.d("GL_ARTEM", "After optimization. Tile free places = " + freeSpaceCount);
        return freeSpace;
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
        int[] xArray = new int[endX - startX + 1];
        for(int i = startX; i <= endX; i++) {
            xArray[i - startX] = i;
        }

        // получаем видимость области в тайловых координатах
        double bottomYViewTile = bottomY / useTileSize;
        double topYViewTile = topY / useTileSize;

        int startY = (int) Math.floor(Math.min(bottomYViewTile, topYViewTile));
        int endY = (int) Math.floor(Math.max(bottomYViewTile, topYViewTile));
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
            // убираем все тайлы прошлого зума
            for(int i = 0; i < renderedTiles.length; i++) {
                Tile tile = renderedTiles[i];
                if(tile != null && tile.getZ() == mapZ) {
                    renderedTiles[i] = null;
                    renderer.setNullPointerForSprite(i);
                }
            }

            mapZ = determinedMapZ;
            useTileSize = determineTileSize();

            int[][][] viewTiles = calcViewTiles();
            renderMap(viewTiles);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        if(event.getPointerCount() == 2) {
            // Маштабирование карты
            double scaleFactor = mapScaleListener.getScaleFactor();
            renderer.setSeeMultiply(scaleFactor);
            updateMapZ(scaleFactor);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            // Перемещение по карте
            float dx = previousX - x;
            float dy = previousY - y;

            dx *= moveStrong;
            dy *= moveStrong;

            double realMove = MathUtils.calculateVectorLength(dx, dy);
            double realMoveLimit = moveStrong * 110;
            //Log.d("GL_ARTEM", String.format("Real move = %.3f moveStrong = %.3f limit = %.3f", realMove, moveStrong, realMoveLimit));
            if(realMove < realMoveLimit) {
                currentMapX += dx;
                currentMapY -= dy;

                int[][][] viewTiles = calcViewTiles();
                renderMap(viewTiles);

                renderer.moveCameraHorizontally(currentMapX, currentMapY);
            }


//            Log.d("GL_ARTEM", "Массив");
//            int[][][] viewTiles = calcViewTiles();
//            for (int i = 0; i < viewTiles.length; i++) {
//                StringBuilder rowStringBuilder = new StringBuilder();
//                for (int j = 0; j < viewTiles[i].length; j++) {
//                    int[] coordinates = viewTiles[i][j];
//                    rowStringBuilder.append(String.format("(%d, %d)", coordinates[0], coordinates[1]) + ", ");
//                }
//                Log.d("GL_ARTEM", rowStringBuilder.toString());
//            }

            //Log.d("GL_ARTEM", "curreentMapX = " + currentMapX + " currentMapY = " + currentMapY);
        }

        //float[] worldLocation = renderer.screenCoordinatesToWorldLocation(x, y);
        //Log.d("GL_ARTEM", String.format("World x = %.3f, y = %.3f", worldLocation[0], worldLocation[1]));

        previousX = x;
        previousY = y;

        requestRender();
        return true;
    }
}
