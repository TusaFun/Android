package com.jupiter.tusa.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jupiter.tusa.MainActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CacheStorage {

    private LruCache<String, Bitmap> memoryCache;
    private DiskLruCache diskLruCache;
    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private MainActivity mainActivity;

    public void setLru(DiskLruCache diskLruCache) {
        this.diskLruCache = diskLruCache;
    }

    public void readyToWork() {
        diskCacheStarting = false;
    }

    public Object getDiskLockObject() {
        return diskCacheLock;
    }

    public CacheStorage(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        File cacheDir = mainActivity.getDiskCacheDir(DISK_CACHE_SUBDIR);
        new InitDiskCacheTask(this).execute(cacheDir);

        final long maxMemory = Runtime.getRuntime().maxMemory();
        final int maxMemoryKilobytes = (int)(maxMemory / 1024);
        Log.d("GL_ARTEM", "maxMemoryKilobytes " + maxMemoryKilobytes);
        memoryCache = new LruCache<String, Bitmap>((int) maxMemoryKilobytes) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                int byteSize = bitmap.getByteCount();
                int useSize = byteSize / 1024;
                Log.d("GL_ARTEM", String.format("Size of key(%s) %d", key, useSize));
                return byteSize / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                Log.d("GL_ARTEM", "Entry removed " + key);
                super.entryRemoved(evicted, key, oldValue, newValue);
            }

            @Override
            public void trimToSize(int maxSize) {
                Log.d("GL_ARTEM", "Trim to size " + maxSize);
                super.trimToSize(maxSize);
            }
        };
    }

    public DiskLruCache.Snapshot getSnapshotFromDiskCache(String key) throws IOException {
        synchronized (diskCacheLock) {
            while(diskCacheStarting) {
                try {
                    diskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (diskLruCache != null) {
                return diskLruCache.get(key);
            }
        }
        return null;
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        // Add to memory cache as before
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
        // Also add to disk cache
        synchronized (diskCacheLock) {
            try {
                if (diskLruCache != null && diskLruCache.get(key) == null) {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);
                    OutputStream outputStream = editor.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    editor.commit();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

}
