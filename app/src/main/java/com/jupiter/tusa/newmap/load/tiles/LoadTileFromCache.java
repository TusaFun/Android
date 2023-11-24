package com.jupiter.tusa.newmap.load.tiles;

import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.cache.template.CacheBytes;

public class LoadTileFromCache {
    private CacheBytes cacheBytes;
    public LoadTileFromCache(CacheStorage cacheStorage) {
        cacheBytes = new CacheBytes(cacheStorage);
    }

    public byte[] loadMvtTile(MvtApiResource mvtApiResource) {
        return cacheBytes.get(mvtApiResource.tileKey());
    }
}
