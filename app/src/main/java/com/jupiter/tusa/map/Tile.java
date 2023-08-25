package com.jupiter.tusa.map;

import android.graphics.Bitmap;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.cache.CacheStorage;
import java.util.concurrent.ExecutorService;

public class Tile {
    private OnTilePrepared onTilePreparedInternal = new OnTilePrepared() {
        @Override
        public void received(Bitmap bitmap, float[] vertexLocations) {
            preparing = false;
            onSpriteReady.ready(bitmap, vertexLocations, useIndex, Tile.this);
        }
    };

    private CacheStorage cacheStorage;
    private OnPrepareSprite onSpriteReady;
    private ExecutorService executorService;
    private MainActivity mainActivity;
    private MyGLRenderer renderer;

    private boolean preparing = true;
    private float tileSize;
    private int tileX;
    private int tileY;
    private int tileZ;
    private int useIndex;

    public Tile(
            MainActivity mainActivity, OnPrepareSprite onSpriteReady,
            ExecutorService executorService, MyGLRenderer renderer,
            int tileX, int tileY, int tileZ, float tileSize, int useIndex
    ) {
        this.tileSize = tileSize;
        this.cacheStorage = mainActivity.getCacheStorage();
        this.onSpriteReady = onSpriteReady;
        this.mainActivity = mainActivity;
        this.executorService = executorService;
        this.renderer = renderer;
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileZ = tileZ;
        this.useIndex = useIndex;
    }

    public int getX() {
        return tileX;
    }

    public int getY() {
        return tileY;
    }

    public int getZ() {
        return tileZ;
    }

    public void render() {
        // определяем где тайл будет в мировых координатах
        float topLeftX = tileX * tileSize;
        float topLeftY = -tileY * tileSize;

        float[] vertexLocations = new float[] {
                topLeftX, topLeftY,
                topLeftX, topLeftY - tileSize,
                topLeftX + tileSize, topLeftY - tileSize,
                topLeftX + tileSize, topLeftY
        };

        int[] tileCoordinates = new int[] { tileX, tileY, tileZ };

        // загружаем или достаем тайл из кэша
        PrepareTileRunnable prepareTileRunnable = new PrepareTileRunnable(cacheStorage, tileCoordinates, vertexLocations, onTilePreparedInternal);
        executorService.submit(prepareTileRunnable);
    }
}
