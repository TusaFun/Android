package com.jupiter.tusa.map;

import android.graphics.Bitmap;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.cache.CacheStorage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Tile {
    private OnTilePrepared onTilePreparedInternal = new OnTilePrepared() {
        @Override
        public void received(Bitmap bitmap, float[] vertexLocations) {
            Tile.this.bitmap = bitmap;
            ready = true;
            onSpriteReady.ready(bitmap, vertexLocations, useIndex, initiator,Tile.this);
        }
    };

    private CacheStorage cacheStorage;
    private OnPrepareSprite onSpriteReady;
    private ExecutorService executorService;
    private MainActivity mainActivity;
    private MyGLRenderer renderer;

    private Bitmap bitmap;
    private boolean ready = false;
    private float tileSize;
    private int tileX;
    private int tileY;
    private int tileZ;
    private int useIndex;
    private Tile[] renderedTiles;
    private RenderTileInitiator initiator;
    private float[] vertexLocations;
    private Future[] prepareTilesFutures;

    public Tile(
            MainActivity mainActivity, OnPrepareSprite onSpriteReady,
            ExecutorService executorService, MyGLRenderer renderer,
            int tileX, int tileY, int tileZ, float tileSize, int useIndex, RenderTileInitiator initiator, Tile[] renderedTiles,
            Future[] prepareTilesFutures
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
        this.initiator = initiator;
        this.renderedTiles = renderedTiles;
        this.prepareTilesFutures = prepareTilesFutures;
    }

    public float[] getVertexLocations() {
        return vertexLocations;
    }

    public int getUseIndex() {
        return useIndex;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean getReady() {
        return ready;
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
        renderedTiles[useIndex] = this;

        // определяем где тайл будет в мировых координатах
        float topLeftX = tileX * tileSize;
        float topLeftY = -tileY * tileSize;

        vertexLocations = new float[] {
                topLeftX, topLeftY,
                topLeftX, topLeftY - tileSize,
                topLeftX + tileSize, topLeftY - tileSize,
                topLeftX + tileSize, topLeftY
        };

        int[] tileCoordinates = new int[] { tileX, tileY, tileZ };

        // загружаем или достаем тайл из кэша
        PrepareTileRunnable prepareTileRunnable = new PrepareTileRunnable(cacheStorage, tileCoordinates, vertexLocations, onTilePreparedInternal);
        Future<?> future = executorService.submit(prepareTileRunnable);
        for(int i = 0; i < prepareTilesFutures.length; i++) {
            if(prepareTilesFutures[i] == null) {
                prepareTilesFutures[i] = future;
                break;
            }
        }
    }
}
