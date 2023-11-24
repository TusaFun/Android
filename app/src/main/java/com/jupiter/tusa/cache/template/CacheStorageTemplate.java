package com.jupiter.tusa.cache.template;

import android.icu.util.Output;
import android.util.LruCache;
import com.jakewharton.disklrucache.DiskLruCache;
import com.jupiter.tusa.cache.CacheStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CacheStorageTemplate<T> {
    private LruCache<String, T> memory;
    protected CacheStorage cacheStorage;
    public CacheStorageTemplate(LruCache<String, T> memory, CacheStorage cacheStorage) {
        this.memory = memory;
        this.cacheStorage = cacheStorage;
    }

    protected abstract T fromInputStream(InputStream inputStream);
    protected abstract void write(OutputStream outputStream, T body) throws IOException;

    public T getFromMem(String key) {
        return memory.get(key);
    }

    public void addToMemCache(String key, T body) {
        if (getFromMem(key) == null) {
            memory.put(key, body);
        }
    }

    public T get(String key) {
        T fromMem = getFromMem(key);
        if(fromMem != null) {
            return fromMem;
        }

        try {
            DiskLruCache.Snapshot snapshot = cacheStorage.getSnapshotFromDiskCache(key);
            if(snapshot == null) {
                return null;
            }

            InputStream inputStream = snapshot.getInputStream(0);
            T body = fromInputStream(inputStream);
            inputStream.close();
            snapshot.close();
            return body;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void addToCache(String key, T body) {
        // Add to memory cache as before
        if (getFromMem(key) == null) {
            memory.put(key, body);
        }
        DiskLruCache diskLruCache = cacheStorage.getDiskLruCache();
        // Also add to disk cache
        synchronized (cacheStorage.getDiskLockObject()) {
            try {
                if (diskLruCache != null && diskLruCache.get(key) == null) {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);
                    OutputStream outputStream = editor.newOutputStream(0);
                    write(outputStream, body);
                    outputStream.flush();
                    outputStream.close();
                    editor.commit();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
