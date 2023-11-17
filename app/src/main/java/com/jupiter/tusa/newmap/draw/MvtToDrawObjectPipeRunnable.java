package com.jupiter.tusa.newmap.draw;

import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.MapSurfaceView;
import com.jupiter.tusa.newmap.draw.prepare.PrepareToDrawMvtData;
import com.jupiter.tusa.newmap.draw.prepare.PrepareToDrawMvtGeom;
import com.jupiter.tusa.newmap.draw.prepare.PreparedLayersObjects;
import com.jupiter.tusa.newmap.load.tiles.MvtApiResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MvtToDrawObjectPipe implements Runnable {
    private CacheBytes cacheBytes;
    private MvtApiResource mvtApiResource;
    private MapSurfaceView mapSurfaceView;
    public MvtToDrawObjectPipe(
            CacheBytes cacheBytes,
            MvtApiResource mvtApiResource,
            MapSurfaceView mapSurfaceView
    ) {
        this.cacheBytes = cacheBytes;
        this.mvtApiResource = mvtApiResource;
        this.mapSurfaceView = mapSurfaceView;
    }

    @Override
    public void run() {
        byte[] tile = cacheBytes.get(mvtApiResource.tileKey());
        if(tile == null) {
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
                tile = outputStream.toByteArray();
                if(responseCode == 200) {
                    String tileKey = mvtApiResource.tileKey();
                    cacheBytes.addToCache(tileKey, tile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PrepareToDrawMvtData prepareToDrawMvtData = new PrepareToDrawMvtData(
                new PreparedLayersObjects(
                        new ArrayList<>(),
                        new ArrayList<>()
                ),
                tile,
                mapSurfaceView.getMapStyle()
        );
        PrepareToDrawMvtGeom prepareToDrawMvtGeom = new PrepareToDrawMvtGeom();
        prepareToDrawMvtGeom.prepareMvt(prepareToDrawMvtData);

        int extent = 4096;
        float worldX =  0;
        float worldY = 0;

        for(float[] vertices : prepareToDrawMvtGeom.allVerticesArrays) {
            for (int i = 0; i < vertices.length / 2; i++) {
                vertices[i * 2] += worldX;
                vertices[i * 2 + 1] += worldY;
            }
        }

        mapSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mapSurfaceView.getMapRenderer().drawPreparedData(prepareToDrawMvtData.drawLayersObjects);
            }
        });
        mapSurfaceView.requestRender();
    }
}
