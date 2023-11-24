package com.jupiter.tusa.newmap.thread.result.handlers;

import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.load.tiles.LoadTileRunnable;

public class SaveMvtToCacheHandler implements RunnableHandler<LoadTileRunnable> {
    private CacheBytes cacheBytes;
    public SaveMvtToCacheHandler(CacheStorage cacheStorage) {
        this.cacheBytes = new CacheBytes(cacheStorage);
    }

    @Override
    public void handle(LoadTileRunnable input) {
        byte[] bytes = input.getBytes();
        String tileKey = input.getKey();
        cacheBytes.addToCache(tileKey, bytes);
    }
}
