package com.jupiter.tusa.newmap.event;

import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.load.tiles.LoadTileRunnable;

public class SaveMvtToCacheEvent implements MapSignatureEvent<LoadTileRunnable> {
    private CacheBytes cacheBytes;
    public SaveMvtToCacheEvent(CacheStorage cacheStorage) {
        this.cacheBytes = new CacheBytes(cacheStorage);
    }

    @Override
    public void handle(LoadTileRunnable input) {
        byte[] bytes = input.getBytes();
        String tileKey = input.getKey();
        cacheBytes.addToCache(tileKey, bytes);
    }
}
