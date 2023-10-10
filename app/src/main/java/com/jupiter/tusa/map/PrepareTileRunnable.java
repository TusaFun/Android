package com.jupiter.tusa.map;

import android.graphics.Bitmap;
import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.cache.OnImageReady;
import com.jupiter.tusa.cache.PrepareImageRunnable;

public class PrepareTileRunnable implements Runnable {
    private String apiUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/%d/%d/%d?access_token=pk.eyJ1IjoiaW52ZWN0eXMiLCJhIjoiY2w0emRzYWx5MG1iMzNlbW91eWRwZzdldCJ9.EAByLTrB_zc7-ytI6GDGBw";
    private final CacheStorage cacheStorage;
    private final int[] params;
    private OnTilePrepared onTileReady;
    private float[] vertexLocations;

    public PrepareTileRunnable(CacheStorage cacheStorage, int[] tileXYZ, float[] vertexLocations, OnTilePrepared onTileReady) {
        this.cacheStorage = cacheStorage;
        this.params = tileXYZ;
        this.onTileReady = onTileReady;
        this.vertexLocations = vertexLocations;
    }

    @Override
    public void run() {
        String imageKey = String.format("tile%d%d%d", params[0], params[1], params[2]);
        String requestUrl = String.format(apiUrl, params[2], params[0], params[1]);
        PrepareImageRunnable prepareImageRunnable = new PrepareImageRunnable(cacheStorage, new OnImageReady() {
            @Override
            public void received(Bitmap image) {
                onTileReady.received(image, vertexLocations);
            }
        }, imageKey, requestUrl);
        prepareImageRunnable.run();
    }
}
