package com.jupiter.tusa.cache;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jupiter.tusa.image.LoadImageRunnable;

import java.io.IOException;
import java.io.InputStream;

public class PrepareImageRunnable implements Runnable{
    private CacheStorage cacheStorage;
    private OnImageReady onImageReady;
    private String imageKey;
    private String loadUrl;

    public PrepareImageRunnable(CacheStorage cacheStorage, OnImageReady onImageReady, String imageKey, String loadUrl) {
        this.cacheStorage = cacheStorage;
        this.onImageReady = onImageReady;
        this.imageKey = imageKey;
        this.loadUrl = loadUrl;
    }

    @Override
    public void run() {
        try {
            Bitmap fromMemCache = cacheStorage.getBitmapFromMemCache(imageKey);
            if(fromMemCache != null) {
                onImageReady.received(fromMemCache);
                return;
            }

            DiskLruCache.Snapshot snapshot = cacheStorage.getSnapshotFromDiskCache(imageKey);
            if(snapshot == null) {
                LoadImageRunnable loadImageRunnable = new LoadImageRunnable(loadUrl);
                loadImageRunnable.run();
                byte[] result = loadImageRunnable.getResult();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                cacheStorage.addBitmapToCache(imageKey, imageBitmap);
                onImageReady.received(imageBitmap);
            } else {
                //Log.d("GL_ARTEM", "Load from cache");
                // Read the data from the snapshot's InputStream and decode it into a Bitmap.
                InputStream inputStream = snapshot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                snapshot.close(); // Remember to close the snapshot when you're done.
                cacheStorage.addBitmapToMemoryCache(imageKey, bitmap);
                onImageReady.received(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
