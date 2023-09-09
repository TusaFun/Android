package com.jupiter.tusa.map.net;


public class LoadTileRunnable implements Runnable {
    private String apiUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/%d/%d/%d?access_token=pk.eyJ1IjoiaW52ZWN0eXMiLCJhIjoiY2w0emRzYWx5MG1iMzNlbW91eWRwZzdldCJ9.EAByLTrB_zc7-ytI6GDGBw";

    private int tileX = 0;
    private int tileY = 0;
    private int zoom = 0;
    private LoadImageRunnable loadImageRunnable;

    public LoadTileRunnable(int tileX, int tileY, int zoom) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.zoom = zoom;
    }

    public byte[] getResult() {
        return loadImageRunnable.getResult();
    }

    public void onResult(OnImageLoaded onTileLoaded) {
        loadImageRunnable.onResult(onTileLoaded);
    }

    @Override
    public void run() {
        String requestUrl = String.format(apiUrl, zoom, tileX, tileY);
        loadImageRunnable = new LoadImageRunnable(requestUrl);
        loadImageRunnable.run();
    }
}
