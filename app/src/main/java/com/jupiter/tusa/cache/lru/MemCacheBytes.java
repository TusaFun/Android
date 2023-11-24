package com.jupiter.tusa.cache.lru;

import android.util.LruCache;

public class MemCacheBytes extends LruCache<String, byte[]> {
    public MemCacheBytes(int maxKBytes) {
        super(maxKBytes);
    }

    @Override
    protected int sizeOf(String key, byte[] bytes) {
        int byteSize = bytes.length;
        return byteSize / 1024; //
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, byte[] oldValue, byte[] newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
    }

    @Override
    public void trimToSize(int maxSize) {
        super.trimToSize(maxSize);
    }
}
