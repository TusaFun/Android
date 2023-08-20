package com.jupiter.tusa.map;

import android.graphics.Bitmap;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.cache.CacheStorage;
import java.util.concurrent.ExecutorService;

public class Tile {
    private int spriteRenderIndex;
    private boolean onMap = false;

    private OnTilePrepared onTilePreparedInternal = new OnTilePrepared() {
        @Override
        public void received(Bitmap bitmap, float[] vertexLocations) {
            onSpriteReady.ready(bitmap, vertexLocations, viewTiles, Tile.this);
        }
    };

    private CacheStorage cacheStorage;
    private OnPrepareSprite onSpriteReady;
    private ExecutorService executorService;
    private MainActivity mainActivity;
    private MyGLRenderer renderer;

    private float tileSize = 1;
    private int tileX;
    private int tileY;
    private int tileZ;
    private int[][][] viewTiles;

    public Tile(
            MainActivity mainActivity, OnPrepareSprite onSpriteReady,
            ExecutorService executorService, MyGLRenderer renderer,
            int tileX, int tileY, int tileZ, int[][][] viewTiles
    ) {
        this.cacheStorage = mainActivity.getCacheStorage();
        this.onSpriteReady = onSpriteReady;
        this.mainActivity = mainActivity;
        this.executorService = executorService;
        this.renderer = renderer;
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileZ = tileZ;
        this.viewTiles = viewTiles;
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

    public int getSpriteRenderIndex() {
        return spriteRenderIndex;
    }

    public void setSpriteRenderIndex(int spriteRenderIndex) {
        this.spriteRenderIndex = spriteRenderIndex;
    }

    public boolean getOnMap() {
        return onMap;
    }

    public void render() {
        float topLeftX = tileX * tileSize;
        float topLeftY = -tileY * tileSize;

        float[] vertexLocations = new float[] {
                topLeftX, topLeftY,
                topLeftX, topLeftY - tileSize,
                topLeftX + tileSize, topLeftY - tileSize,
                topLeftX + tileSize, topLeftY
        };

        int[] tileCoordinates = new int[] { tileX, tileY, tileZ };
        PrepareTileRunnable prepareTileRunnable = new PrepareTileRunnable(cacheStorage, tileCoordinates, vertexLocations, viewTiles, onTilePreparedInternal);
        executorService.submit(prepareTileRunnable);
    }
}
