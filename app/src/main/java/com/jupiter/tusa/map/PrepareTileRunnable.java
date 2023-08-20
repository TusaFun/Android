package com.jupiter.tusa.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.map.net.LoadTileRunnable;

import java.io.IOException;
import java.io.InputStream;

public class PrepareTileRunnable implements Runnable {
    private final CacheStorage cacheStorage;
    private final int[] params;
    private OnTilePrepared onTileReady;
    private float[] vertexLocations;
    private int[][][] viewTiles;

    public PrepareTileRunnable(CacheStorage cacheStorage, int[] tileXYZ, float[] vertexLocations, int[][][] viewTiles, OnTilePrepared onTileReady) {
        this.cacheStorage = cacheStorage;
        this.params = tileXYZ;
        this.onTileReady = onTileReady;
        this.vertexLocations = vertexLocations;
        this.viewTiles = viewTiles;
    }

    @Override
    public void run() {
        String imageKey = String.format("tile%d%d%d", params[0], params[1], params[2]);
        try {
            Bitmap fromMemCache = cacheStorage.getBitmapFromMemCache(imageKey);
            if(fromMemCache != null) {
                onTileReady.received(fromMemCache, vertexLocations);
                return;
            }

            DiskLruCache.Snapshot snapshot = cacheStorage.getSnapshotFromDiskCache(imageKey);
            if(snapshot == null) {
                Log.d("GL_ARTEM", "Load tile from network");
                LoadTileRunnable loadTileRunnable = new LoadTileRunnable(params[0], params[1], params[2]);
                loadTileRunnable.run();
                byte[] result = loadTileRunnable.getResult();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                cacheStorage.addBitmapToCache(imageKey, imageBitmap);
                onTileReady.received(imageBitmap, vertexLocations);
            } else {
                //Log.d("GL_ARTEM", "Load from cache");
                // Read the data from the snapshot's InputStream and decode it into a Bitmap.
                InputStream inputStream = snapshot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                snapshot.close(); // Remember to close the snapshot when you're done.
                cacheStorage.addBitmapToMemoryCache(imageKey, bitmap);
                onTileReady.received(bitmap, vertexLocations);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
