package com.jupiter.tusa.cache;

import android.os.AsyncTask;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

public class InitDiskCacheTask extends AsyncTask<File, Void, Void> {

    private CacheStorage cacheStorage;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    public InitDiskCacheTask(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    @Override
    protected Void doInBackground(File... params) {
        synchronized (cacheStorage.getDiskLockObject()) {
            File cacheDir = params[0];
            try {
                DiskLruCache diskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                cacheStorage.setLru(diskLruCache);
                cacheStorage.readyToWork();
                cacheStorage.getDiskLockObject().notifyAll();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return null;
    }
}
