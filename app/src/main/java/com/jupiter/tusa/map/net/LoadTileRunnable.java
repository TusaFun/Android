package com.jupiter.tusa.map.net;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadTileRunnable implements Runnable {

    private String apiUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/%d/%d/%d?access_token=pk.eyJ1IjoiaW52ZWN0eXMiLCJhIjoiY2w0emRzYWx5MG1iMzNlbW91eWRwZzdldCJ9.EAByLTrB_zc7-ytI6GDGBw";
    private byte[] result;
    private OnTileLoaded onTileLoaded;

    private int tileX = 0;
    private int tileY = 0;
    private int zoom = 0;

    public LoadTileRunnable(int tileX, int tileY, int zoom) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.zoom = zoom;
    }

    public byte[] getResult() {
        return result;
    }

    public void onResult(OnTileLoaded onTileLoaded) {
        this.onTileLoaded = onTileLoaded;
    }

    @Override
    public void run() {
        try {
            String requestUrl = String.format(apiUrl, zoom, tileX, tileY);
            URL url = new URL(requestUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();
            int nRead;
            byte[] data = new byte[1024];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while((nRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
            outputStream.flush();
            byte[] bytes = outputStream.toByteArray();
            result = bytes;
            if(this.onTileLoaded != null) {
                this.onTileLoaded.execute(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
