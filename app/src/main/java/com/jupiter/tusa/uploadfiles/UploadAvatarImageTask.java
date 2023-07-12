package com.jupiter.tusa.uploadfiles;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadAvatarImageTask extends AsyncTask<Object, Object, String> {
    private final byte[] fileBytes;
    private final String accessToken;
    private final String fileType = "AVATAR";

    private final String TAG = "tusa-upload-file";

    public UploadAvatarImageTask(byte[] fileBytes, String accessToken) {
        this.fileBytes = fileBytes;
        this.accessToken = accessToken;
    }

    @Override
    protected String doInBackground(Object[] objects) {
        String uploadFileUrl = "https://tusa.fun/requests-service/api/files/upload";

        HttpURLConnection connection = null;
        try {
            URL url = new URL(uploadFileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(fileBytes.length);
            connection.setRequestProperty("Content-Type", "image/jpeg");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Tusa-File-Type", fileType);
            try (OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {
                outputStream.write(fileBytes);
                Log.d(TAG, "File size " + fileBytes.length);
                outputStream.flush();
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Connection response code " + responseCode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream())
            ) {
                int data;
                while ((data = inputStream.read()) != -1) {
                    byteArrayOutputStream.write(data);
                }
                return byteArrayOutputStream.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

    }
}
