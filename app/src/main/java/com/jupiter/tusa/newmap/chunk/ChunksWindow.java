package com.jupiter.tusa.newmap.chunk;

import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.MapSurfaceView;
import com.jupiter.tusa.newmap.draw.MapStyle;
import com.jupiter.tusa.newmap.draw.MvtToDrawObjectPipeRunnable;
import com.jupiter.tusa.newmap.load.tiles.MvtApiResource;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChunksWindow {
    Executor executor = Executors.newFixedThreadPool(2);
    CacheBytes cacheBytes;
    private MapSurfaceView mapSurfaceView;

    private int windowX = 0;
    private int windowY = 0;
    private int windowZ = 0;
    private TileChunk[][][] chunks;
    private int windowXSize;
    private int windowYSize;

    public ChunksWindow(int initWindowXSize, int initWindowYSize, MapSurfaceView mapSurfaceView) {
        this.mapSurfaceView = mapSurfaceView;
        this.windowXSize = initWindowXSize;
        this.windowYSize = initWindowYSize;
        chunks = new TileChunk[10][initWindowXSize][initWindowYSize];
    }

    public void show() {
        for(int x = windowX; x <= windowX + windowXSize; x++) {
            for(int y = windowY; y <= windowY + windowYSize; y++) {
                MvtApiResource mvtApiResource = new MvtApiResource(windowZ, x, y);
                executor.execute(new MvtToDrawObjectPipeRunnable(
                        cacheBytes,
                        mvtApiResource,
                        mapSurfaceView.getMapStyle(),
                        loadCounter,
                        onTilePipelineReady
                ));
            }
        }

    }

    public void shiftWindow(int dx, int dy, int dz) {
        windowX += dx;
        windowY += dy;

        TileChunk[][] currentChunk = chunks[0];
        for(int x = 0; x < windowXSize; x++) {
            for(int y = 0; y < windowYSize; y++) {
                currentChunk[x][y] = new TileChunk();
            }
        }
        currentChunk = shiftChunk(-2, -2, currentChunk);


    }

    private TileChunk[][] shiftChunk(int dx, int dy, TileChunk[][] currentChunk) {
        if(Math.abs(dx) >= windowXSize || Math.abs(dy) >= windowYSize) {
            return new TileChunk[windowXSize][windowYSize];
        }

        if(dx > 0) {
            for(int x = 0; x < windowXSize; x++) {
                if(x + dx >= windowXSize) {
                    currentChunk[x] = new TileChunk[windowYSize];
                    continue;
                }
                currentChunk[x] = currentChunk[x + dx];
            }
        } else if(dx < 0) {
            for(int x = windowXSize - 1; x >= 0; x--) {
                if(x < -dx) {
                    currentChunk[x] = new TileChunk[windowYSize];
                    continue;
                }
                currentChunk[x] = currentChunk[x + dx];
            }
        }

        if(dy > 0) {
            for(int x = 0; x < windowXSize; x++) {
                for(int y = 0; y < windowYSize; y++) {
                    if(y + dy >= windowYSize) {
                        currentChunk[x][y] = null;
                        continue;
                    }
                    currentChunk[x][y] = currentChunk[x][y + dy];
                }
            }
        } else if(dy < 0) {
            for(int x = 0; x < windowXSize; x++) {
                for(int y = windowYSize - 1; y >= 0; y--) {
                    if(y < -dy) {
                        currentChunk[x][y] = null;
                        continue;
                    }
                    currentChunk[x][y] = currentChunk[x][y + dy];
                }
            }
        }

        return currentChunk;
    }

}
