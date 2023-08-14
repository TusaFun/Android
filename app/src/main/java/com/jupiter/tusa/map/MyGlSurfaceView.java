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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class MyGlSurfaceView extends GLSurfaceView {
    public int getMapZ() {
        return mapZ;
    }

    private final MyGLRenderer renderer;
    private MainActivity mainActivity;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    // Применение тайлов после загрузки
    private OnTilePrepared onTilePrepared = new OnTilePrepared() {
        @Override
        public void received(Bitmap tile, float[] vertexLocations, int tileIndexRenderArray, int totalTilesWaitForLoad) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.renderTile(tile, vertexLocations, tileIndexRenderArray, totalTilesWaitForLoad);
                }
            });
            requestRender();
        }
    };

    // Маштабирование карты
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;
    private int mapZ = 0;
    private float defaultFov = 45f;

    // Перемещение карты
    private float previousX = 0f;
    private float previousY = 0f;
    private float currentMapX = 0f;
    private float currentMapY = 0f;
    private float moveStrong = 500f;
    private float moveStepDeltaBorder = 1f;

    public MyGlSurfaceView(Context context, AttributeSet attributeSet) throws ExecutionException, InterruptedException, TimeoutException {
        super(context, attributeSet);
        mainActivity = (MainActivity) context;
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context, this);
        setRenderer(renderer);

        mapScaleListener = new MapScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void renderMap(int z) {
        mapZ = z;
        if(z == 0) {
            PrepareTileRunnable prepareTileRunnable = new PrepareTileRunnable(
                    mainActivity.getCacheStorage(),
                    new int[] { 0, 0, 0 },
                    new float[] {
                            -1f, 1f,
                            -1f, -1f,
                            1f, -1f,
                            1f, 1f
                    },
                    0,
                    1,
                    onTilePrepared
            );
            executorService.submit(prepareTileRunnable);
        } else if(z == 1) {
            // top left
            showTile(new int[] { 0, 0, z }, new float[] {
                    -1f, 1f,
                    -1f, 0f,
                    0f, 0f,
                    0f, 1f
            },  0, 4);
            // bottom left
            showTile(new int[] { 0, 1, z }, new float[] {
                    -1f, 0f,
                    -1f, -1f,
                    0f, -1f,
                    0f, 0f
            },  1, 4);
            // bottom right
            showTile(new int[] { 1, 1, z }, new float[] {
                    0f, 0f,
                    0f, -1f,
                    1f, -1f,
                    1f, 0f
            },  2, 4);
            //top right
            showTile(new int[] { 1, 0, z }, new float[] {
                    0f, 1f,
                    0f, 0f,
                    1f, 0f,
                    1f, 1f
            },  3, 4);
        } else {
            WorldBounds viewportWorldBounds = renderer.calcWorldLocationViewportBounds();
            LatLonBounds latLonBounds = renderer.calcLatLonBounds(viewportWorldBounds);
            TilesBounds tilesBounds = renderer.calcTilesBounds(latLonBounds, z);

            // Это стандартный размер карты в данном openGL проекте
            float worldXWidth = 2;
            float worldYHeight = 2;

            int topLeftX = tilesBounds.topLeftX;
            int topLeftY = tilesBounds.topLeftY;
            int bottomRightX = tilesBounds.bottomRightX;
            int bottomRightY = tilesBounds.bottomRightY;

            int mapBorderSizeInTiles = (int) Math.pow(2, z);
            if(topLeftY == 0 && bottomRightY == 0) {
                bottomRightY = mapBorderSizeInTiles;
            }

            if(topLeftX == 0 && bottomRightX == 0) {
                bottomRightX = mapBorderSizeInTiles;
            }

            int tilesXCount = Math.abs(topLeftX - bottomRightX) + 1;
            int tilesYCount = Math.abs(topLeftY - bottomRightY) + 1;
            int total = tilesXCount * tilesYCount;
            int renderIndex = 0;

            float worldSpriteWidth = worldXWidth / mapBorderSizeInTiles;
            float worldSpriteHeight = worldYHeight / mapBorderSizeInTiles;

            for(int x = topLeftX; x <= bottomRightX; x++) {
                for(int y = topLeftY; y <= bottomRightY; y++) {
                    Log.d("GL_ARTEM", String.format("render tileX=%d tileY=%d", x, y));

                    float topLeftXVertex = x * worldSpriteWidth - 1;
                    float topLeftYVertex = 1 - y * worldSpriteHeight;
                    showTile(new int[] { x, y, z }, new float[] {
                            topLeftXVertex, topLeftYVertex,
                            topLeftXVertex, topLeftYVertex - worldSpriteHeight,
                            topLeftXVertex + worldSpriteWidth, topLeftYVertex - worldSpriteHeight,
                            topLeftXVertex + worldSpriteWidth, topLeftYVertex
                    },  renderIndex, total);

                    renderIndex++;
                }
            }

            //Log.d("GL_ARTEM", String.format("tilesXCount = %d, tilesYCount = %d, worldXWidth = %.3f, worldYHeight = %.3f", tilesXCount, tilesYCount, worldXWidth, worldYHeight));
        }
    }

    private void showTile(int[] tileCoordinates, float[] vertexCoordinates, int renderIndex, int totalTiles) {
        PrepareTileRunnable prepareTileRunnable = new PrepareTileRunnable(
                mainActivity.getCacheStorage(),
                tileCoordinates,
                vertexCoordinates,
                renderIndex,
                totalTiles,
                onTilePrepared
        );
        executorService.submit(prepareTileRunnable);
    }

    private void updateMapByZIfNeed() {
        //Log.d("GL_ARTEM", "FOV = " + renderer.getFov());
        int z;
        if(renderer.getFov() > 30) {
            z = 0;
        } else if(renderer.getFov() > 20){
            z = 1;
        } else if(renderer.getFov() > 14){
            z = 2;
        } else if(renderer.getFov() > 5){
            z = 3;
        } else if(renderer.getFov() > 2){
            z = 4;
        } else if(renderer.getFov() > 1){
            z = 5;
        } else {
            z = 6;
        }

        Log.d("GL_ARTEM", "fov = " + renderer.getFov());

        if(getMapZ() != z) {
            Log.d("GL_ARTEM", "UPDATE MAP TILES");
            renderMap(z);
        }
    }

    private float getMoveStrongDivider() {
        float k;
        if(renderer.getFov() > 30) {
            k = 1;
        } else if(renderer.getFov() > 20){
            k = 2;
        } else if(renderer.getFov() > 14){
            k = 3;
        } else if(renderer.getFov() > 5){
            k = 4;
        } else if(renderer.getFov() > 2){
            k = 5;
        } else {
            k = 10;
        }
        return k;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        final float x = event.getX();
        final float y = event.getY();

        if(event.getPointerCount() == 2) {
            // Маштабирование карты
            float scaleFactor = mapScaleListener.getScaleFactor();
            renderer.setFov(defaultFov / scaleFactor);
            // Если нужно тайлы другие грузить
            updateMapByZIfNeed();
        } else if(event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1){
            // Перемещение по карте
            float dx = previousX - x;
            float dy = previousY - y;
            float useMoveStrong = moveStrong * getMoveStrongDivider();

            dx /= useMoveStrong;
            dy /= useMoveStrong;

            if(MathUtils.calculateVectorLength(dx, dy) < moveStepDeltaBorder) {
                currentMapX += dx;
                currentMapY -= dy;

                renderer.moveCameraHorizontally(currentMapX, currentMapY);
            }

            //float[] worldCoordinates = renderer.screenCoordinatesToWorldLocation(x, y);
            //Log.d("GL_ARTEM", String.format("World x=%.3f y=%.3f", worldCoordinates[0], worldCoordinates[1]));
        }

        previousX = x;
        previousY = y;

        requestRender();
        return true;
    }
}
