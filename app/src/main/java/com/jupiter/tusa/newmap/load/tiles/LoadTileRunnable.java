package com.jupiter.tusa.newmap.load.tiles;

import com.jupiter.tusa.newmap.thread.result.handlers.RunnableHandler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadTileRunnable implements Runnable {
    public LoadTileRunnable(MvtApiResource mvtApiResource, RunnableHandler<LoadTileRunnable>[] handlers) {
        this.mvtApiResource = mvtApiResource;
        this.handlers = handlers;
    }

    public MvtApiResource getMvtApiResource() {
        return mvtApiResource;
    }

    public String getKey() {
        return mvtApiResource.tileKey();
    }

    public byte[] getBytes() {
        return bytes;
    }

    private RunnableHandler<LoadTileRunnable>[] handlers;
    private MvtApiResource mvtApiResource;
    private byte[] bytes;

    @Override
    public void run() {
        try {
            URL url = new URL(mvtApiResource.makeUrl());
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
            bytes = outputStream.toByteArray();
            if(responseCode == 200) {
                for(RunnableHandler<LoadTileRunnable> handler : handlers) {
                    handler.handle(this);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
