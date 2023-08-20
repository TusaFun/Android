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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyGlSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private MainActivity mainActivity;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    // Применение тайлов после загрузки
    private OnPrepareSprite onSpriteReady = new OnPrepareSprite() {
        @Override
        public void ready(Bitmap bitmap, float[] vertexLocations, int[][][] viewTiles, Tile tile) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    int freeIndex = renderer.getFreeSpriteIndex();
                    if(freeIndex == -1) {
                        optimizeTiles(viewTiles);
                        freeIndex = renderer.getFreeSpriteIndex();
                        assert freeIndex != -1;
                    }
                    Sprite sprite = new Sprite(mainActivity, bitmap, freeIndex, vertexLocations);
                    tile.setSpriteRenderIndex(freeIndex);
                    renderedTiles[freeIndex] = tile;
                    renderer.renderSprite(sprite, freeIndex);
                }
            });
            requestRender();
        }
    };

    // Маштабирование карты
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;

    // Перемещение карты
    private float previousX = 0f;
    private float previousY = 0f;
    private float currentMapX = 0;
    private float currentMapY = 0;
    private float moveStrong = 1000f;
    private float moveStepDeltaBorder = 1f;
    private int currentMapXInt = (int) currentMapX;
    private int currentMapYInt = (int) currentMapY;

    // Отрисовка карты
    final int tilesSize = 64;
    private int mapZ = 3;
    private Tile[] renderedTiles = new Tile[tilesSize];

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

        mapScaleListener = new MapScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void renderMap(int[][][] viewTiles) {
        Log.d("GL_ARTEM", "Render map");
        // Отрендрить тайлы которые должны быть видимы
        for(int xIndex = 0; xIndex < viewTiles.length; xIndex++) {
            for(int yIndex = 0; yIndex < viewTiles[xIndex].length; yIndex++) {
                int[] tileCoordinates = viewTiles[xIndex][yIndex];
                int tileX = tileCoordinates[0];
                int tileY = tileCoordinates[1];
                int tileZ = tileCoordinates[2];

                boolean exist = false;
                for(int i = 0; i < renderedTiles.length; i++) {
                    Tile tile = renderedTiles[i];
                    if(tile == null)
                        continue;
                    if(tile.getX() == tileX && tile.getY() == tileY) {
                        if(tile.getZ() == tileZ && !exist) {
                            exist = true;
                            continue;
                        }
                        // Все другие тайлы с таким же X и Y убираем
                        renderer.setNullPointerForSprite(tile.getSpriteRenderIndex());
                        renderedTiles[tile.getSpriteRenderIndex()] = null;
                    }
                }
                if(exist)
                    continue;

                Tile tile = new Tile(mainActivity, onSpriteReady, executorService, renderer, tileX, tileY, mapZ, viewTiles);
                tile.render();
            }
        }
    }

    public void optimizeTiles(int[][][] viewTiles) {
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
                renderer.setNullPointerForSprite(renderedTile.getSpriteRenderIndex());
                renderedTiles[renderedTile.getSpriteRenderIndex()] = null;
            }
        }

        int freeSpace = 0;
        for(int i = 0; i < renderedTiles.length; i++) {
            if(renderedTiles[i] == null) {
                freeSpace++;
            }
        }

        Log.d("GL_ARTEM", "After optimization. Tile free places = " + freeSpace);
    }

    // тут определяется видимая область
    // отдается массив где первый элемент(первая размерность трехмерного массива) x(ширина) второй y(высота),
    // а в третьем храняться координаты тайла x y
    // используются координаты тайлан а третьем элементе
    public int[][][] calcViewTiles() {
        // Текущая видимая область в мировых координатах
        float[] leftTopWorld = renderer.screenCoordinatesToWorldLocation(0f, 0f);
        float[] bottomRightWorld = renderer.screenCoordinatesToWorldLocation(renderer.getWidth(), renderer.getHeight());

        int maxTileXOrY = (int) Math.pow(2, mapZ) - 1;

        // Технически видим область больше чтобы она успевала прогружаться и пользователь видел
        // меньше загрузок тайлов, артефактов.
        float topLeftPreViewing = 1;
        float leftX = leftTopWorld[0] - topLeftPreViewing;
        float topY = leftTopWorld[1] + topLeftPreViewing;
        if(leftX < 0) {
            leftX = 0;
        }
        if(topY > 0) {
            topY = 0;
        } else {
            topY = -1 * topY;
        }

        float rightX = bottomRightWorld[0];
        float bottomY = bottomRightWorld[1];

        if(rightX < 0) {
            rightX = 0;
        }

        if(bottomY > 0) {
            bottomY = 0;
        } else {
            bottomY = -1 * bottomY;
        }

        if(bottomY > maxTileXOrY) {
            bottomY = maxTileXOrY;
        }

        if(rightX > maxTileXOrY) {
            rightX = maxTileXOrY;
        }

        int startX = (int) Math.floor(Math.min(leftX, rightX));
        int endX = (int) Math.ceil(Math.max(leftX, rightX));
        int[] xArray = new int[endX - startX + 1];
        for(int i = startX; i <= endX; i++) {
            xArray[i - startX] = i;
        }

        int startY = (int) Math.floor(Math.min(bottomY, topY));
        int endY = (int) Math.ceil(Math.max(bottomY, topY));
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

    public void updateMapZ(float scaleFactor) {
        Log.d("GL_ARTEM", "Scale factor = " + scaleFactor);
        if(scaleFactor < 0.9) {
            mapZ = 4;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        if(event.getPointerCount() == 2) {
            // Маштабирование карты
            float scaleFactor = mapScaleListener.getScaleFactor();
            renderer.setSeeMultiply(scaleFactor);
            updateMapZ(scaleFactor);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            // Перемещение по карте
            float dx = previousX - x;
            float dy = previousY - y;

            dx /= moveStrong;
            dy /= moveStrong;

            if(MathUtils.calculateVectorLength(dx, dy) < moveStepDeltaBorder) {
                currentMapX += dx;
                currentMapY -= dy;

                // это небольшая оптимизация
                // мы пробуем рендрить карту только тогда когда пользователь прокрутил тайл
                // один тайл занимает квадрат стороной в одну едницу мировых координат
                int mapXInt = (int) (currentMapX);
                int mapYInt = (int) (currentMapY);

                if(currentMapXInt != mapXInt || currentMapYInt != mapYInt) {
                    int[][][] viewTiles = calcViewTiles();
                    renderMap(viewTiles);
                }

                currentMapYInt = mapYInt;
                currentMapXInt = mapXInt;
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
