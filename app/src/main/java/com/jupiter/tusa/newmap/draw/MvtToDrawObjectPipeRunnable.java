package com.jupiter.tusa.newmap.draw;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jupiter.tusa.cache.template.CacheBytes;
import com.jupiter.tusa.newmap.MapSurfaceView;
import com.jupiter.tusa.newmap.TileWorldCoordinates;
import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicInput;
import com.jupiter.tusa.newmap.load.tiles.MvtApiResource;
import com.jupiter.tusa.newmap.mvt.MvtLines;
import com.jupiter.tusa.newmap.mvt.MvtObject;
import com.jupiter.tusa.newmap.mvt.MvtObjectStyled;
import com.jupiter.tusa.newmap.mvt.MvtPolygon;
import com.jupiter.tusa.newmap.mvt.MvtUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import vector_tile.VectorTile;

public class MvtToDrawObjectPipeRunnable implements Runnable {
    private final CacheBytes cacheBytes;
    private final MvtApiResource mvtApiResource;
    private final MapSurfaceView mapSurfaceView;
    private final CountDownLatch countDownLatchReady;
    public MvtToDrawObjectPipeRunnable(
            CacheBytes cacheBytes,
            MvtApiResource mvtApiResource,
            MapSurfaceView mapSurfaceView,
            CountDownLatch countDownLatchReady
    ) {
        this.cacheBytes = cacheBytes;
        this.mvtApiResource = mvtApiResource;
        this.mapSurfaceView = mapSurfaceView;
        this.countDownLatchReady = countDownLatchReady;
    }

    @Override
    public void run() {
        // GET TILE
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
        if(tile == null) {
            countDownLatchReady.countDown();
            return;
        }
        // END GET TILE


        // READ MVT VECTOR DATA
        MapStyleList mapStyleList = mapSurfaceView.getMapStyle();
        MapStyle mapStyle = mapStyleList.getStyleFor(mvtApiResource.z);
        VectorTile.Tile mvtTile = null;
        try {
           mvtTile = MvtUtils.parse(tile);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        assert mvtTile != null;

        List<MvtObject> mvtObjects = new ArrayList<>();
        List<VectorTile.Tile.Layer> layers = mvtTile.getLayersList();
        for(VectorTile.Tile.Layer layer : layers) {
            if(!mapStyle.showLayers.contains(layer.getName())) {
                //Log.d("GL_ARTEM", String.format("layer skipped %s", layer.getName()));
                continue;
            }

            for(VectorTile.Tile.Feature feature : layer.getFeaturesList()) {
                if(feature.getType() == VectorTile.Tile.GeomType.POLYGON) {
                    mvtObjects.addAll(MvtUtils.readPolygons(feature, layer));
                } else if(feature.getType() == VectorTile.Tile.GeomType.LINESTRING) {
                    mvtObjects.addAll(MvtUtils.readLines(feature, layer));
                }
            }
        }
        // END READ MVT VECTOR DATA


        // MOVE TILE
        int tileZ = mvtApiResource.z;
        int tileX = mvtApiResource.x;
        int tileY = mvtApiResource.y;
        TileWorldCoordinates tileWorldCoordinates = new TileWorldCoordinates();
        for(MvtObject mvtObject: mvtObjects) {
            tileWorldCoordinates.applyToTileMvt(mvtObject, tileX, tileY, tileZ);
        }
        // END MOVE TILE


        // STYLE MAP
        List<MvtObjectStyled> mvtObjectStyledList = new ArrayList<>();
        for (MvtObject mvtObject : mvtObjects) {
            float[] color = mapStyle.getColor(mvtObject.getLayerName());
            mvtObjectStyledList.add(new MvtObjectStyled(mvtObject, color));
        }
        // END STYLE MAP


        // Convert to data input objects for OpenGL
        List<FDOFloatBasicInput> fdoFloatBasicInputs = new ArrayList<>();
        for(MvtObjectStyled mvtObjectStyled : mvtObjectStyledList) {
            MvtObject mvtObject = mvtObjectStyled.getMvtObject();
            if(mvtObject instanceof MvtPolygon) {
                MvtPolygon polygon = (MvtPolygon) mvtObject;
                fdoFloatBasicInputs.add(new FDOFloatBasicInput(
                        polygon.getVertices(),
                        polygon.triangles,
                        2,
                        4,
                        (short)0,
                        mvtObjectStyled.getColor()
                ));
            } else if(mvtObject instanceof MvtLines) {
                MvtLines lines = (MvtLines) mvtObject;
                fdoFloatBasicInputs.add(new FDOFloatBasicInput(
                        lines.getVertices(),
                        new int[] {},
                        2,
                        4,
                        (short)1,
                        mvtObjectStyled.getColor()
                ));
            }
        }
        // END Convert to data input objects for OpenGL

        mapSurfaceView.getMapRenderer().getDrawFrame().addRenderProgramsInput(fdoFloatBasicInputs);
        countDownLatchReady.countDown();
    }
}
