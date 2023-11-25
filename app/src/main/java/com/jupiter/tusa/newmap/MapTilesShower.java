package com.jupiter.tusa.newmap;

import androidx.core.math.MathUtils;

import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.draw.MvtToDrawObjectPipeRunnable;
import com.jupiter.tusa.newmap.load.tiles.AllTilesLoadPipelinesReadyRunnable;
import com.jupiter.tusa.newmap.load.tiles.MvtApiResource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MapTilesShower {
    Executor executor = Executors.newFixedThreadPool(6);
    Executor executorWaitPatch = Executors.newSingleThreadExecutor();
    CacheBytes cacheBytes;
    private final MapSurfaceView mapSurfaceView;
    private CountDownLatch patchTilesReadyCount;
    private boolean showTestTile = false;

    public MapTilesShower(
            MapSurfaceView mapSurfaceView
    ) {
        this.mapSurfaceView = mapSurfaceView;
        this.cacheBytes = new CacheBytes(mapSurfaceView.getCacheStorage());
    }

    public void nextMapState(MapTilesShowerMapState state) {
        if(showTestTile) {
            patchTilesReadyCount = new CountDownLatch(1);
            executorWaitPatch.execute(new AllTilesLoadPipelinesReadyRunnable(patchTilesReadyCount, mapSurfaceView));
            nextTile(2, 1, 0);
            return;
        }
        MapWorldCamera camera = state.getMapWorldCamera();
        float[] bottomRightCorner = camera.getBottomRightWorldViewCoordinates();
        float[] upperLeftCorner = camera.getUpperLeftWorldViewCoordinates();

        int tileZ = state.getTileZ();
        int borderAmountTiles = (int) Math.pow(2, tileZ);
        int maxTileNumber = borderAmountTiles - 1;

        TileWorldCoordinates tileWorldCoordinates = new TileWorldCoordinates();
        float extent = tileWorldCoordinates.getTileZExtent(tileZ);
        float startX = upperLeftCorner[0];
        float endX = bottomRightCorner[0];
        float startY = -1 * upperLeftCorner[1];
        float endY = -1 * bottomRightCorner[1];

        int startXTile = (int) (startX / extent);
        startXTile = MathUtils.clamp(startXTile, 0, maxTileNumber);
        int endXTile = (int) (endX / extent);
        endXTile = MathUtils.clamp(endXTile, 0, maxTileNumber);
        int startYTile = (int) (startY / extent);
        startYTile = MathUtils.clamp(startYTile, 0, maxTileNumber);
        int endYTile = (int) (endY / extent);
        endYTile = MathUtils.clamp(endYTile, 0, maxTileNumber);

        int amount = (endXTile - startXTile + 1) * (endYTile - startYTile + 1);

        patchTilesReadyCount = new CountDownLatch(amount);
        executorWaitPatch.execute(new AllTilesLoadPipelinesReadyRunnable(patchTilesReadyCount, mapSurfaceView));

        for(int x = startXTile; x <= endXTile; x++) {
            for(int y = startYTile; y <= endYTile; y++) {
                nextTile(tileZ, x, y);
            }
        }
    }

    private void nextTile(int zoom, int x, int y) {
        MvtApiResource mvtApiResource = new MvtApiResource(zoom, x, y);
        executor.execute(new MvtToDrawObjectPipeRunnable(
                cacheBytes,
                mvtApiResource,
                mapSurfaceView,
                patchTilesReadyCount
        ));
    }
}
