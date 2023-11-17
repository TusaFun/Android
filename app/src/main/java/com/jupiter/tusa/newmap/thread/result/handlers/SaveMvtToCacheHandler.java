package com.jupiter.tusa.map.thread.result.handlers;

import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.newmap.load.tiles.LoadTileRunnable;

public class SaveMvtToCacheHandler implements RunnableHandler<LoadTileRunnable> {
    private CacheStorage cacheStorage;
    public SaveMvtToCacheHandler(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    @Override
    public void handle(LoadTileRunnable input) {
        byte[] bytes = input.getBytes();
        String tileKey = input.getKey();

    }
}
