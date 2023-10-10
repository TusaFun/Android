package com.jupiter.tusa.image;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadImageRunnable implements Runnable{
    private String requestUrl;
    private byte[] result;
    private OnImageLoaded onImageLoaded;

    public LoadImageRunnable(String url) {
        requestUrl = url;
    }

    public void onResult(OnImageLoaded onImageLoaded) {
        this.onImageLoaded = onImageLoaded;
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    public void run() {
        try {
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
            if(this.onImageLoaded != null) {
                this.onImageLoaded.execute(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
