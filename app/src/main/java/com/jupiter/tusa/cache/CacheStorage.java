package com.jupiter.tusa.cache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jupiter.tusa.MainActivity;
import java.io.File;
import java.io.IOException;

public class CacheStorage {
    private DiskLruCache diskLruCache;
    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    private final int maxMemoryKilobytes;

    public void setLru(DiskLruCache diskLruCache) {
        this.diskLruCache = diskLruCache;
    }
    public void readyToWork() {
        diskCacheStarting = false;
    }

    public DiskLruCache getDiskLruCache() {return  diskLruCache; }
    public Object getDiskLockObject() {
        return diskCacheLock;
    }
    public int getMaxMemoryKilobytes() {return maxMemoryKilobytes;}

    public CacheStorage(MainActivity mainActivity) {
        File cacheDir = mainActivity.getDiskCacheDir(DISK_CACHE_SUBDIR);
        new InitDiskCacheTask(this).execute(cacheDir);

        final long maxMemory = Runtime.getRuntime().maxMemory();
        maxMemoryKilobytes = (int)(maxMemory / 1024);
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


}
