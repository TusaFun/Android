package com.jupiter.tusa.cache.lru;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MemCacheBitmap extends LruCache<String, Bitmap> {
    public MemCacheBitmap(int maxKBytes) {
        super(maxKBytes);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        int byteSize = bitmap.getByteCount();
        return byteSize / 1024; // KB
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        //Log.d("GL_ARTEM", "Entry removed " + key);
        super.entryRemoved(evicted, key, oldValue, newValue);
    }

    @Override
    public void trimToSize(int maxSize) {
        //Log.d("GL_ARTEM", "Trim to size " + maxSize);
        super.trimToSize(maxSize);
    }
}
