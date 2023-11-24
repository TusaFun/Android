package com.jupiter.tusa.newmap.load.tiles;

import android.util.Log;

import com.jupiter.tusa.newmap.MapSurfaceView;
import java.util.concurrent.CountDownLatch;

public class AllTilesLoadPipelinesReadyRunnable implements Runnable {
    private final CountDownLatch allTilesPipelinesReady;
    private final MapSurfaceView mapSurfaceView;
    public AllTilesLoadPipelinesReadyRunnable(CountDownLatch allTilesPipelinesReady, MapSurfaceView mapSurfaceView) {
        this.allTilesPipelinesReady = allTilesPipelinesReady;
        this.mapSurfaceView = mapSurfaceView;
    }
    @Override
    public void run() {
        try {
            allTilesPipelinesReady.await();
            mapSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mapSurfaceView.getMapRenderer().getDrawFrame().createOpenGlDrawPrograms();
                }
            });
            mapSurfaceView.requestRender();
            Log.d("GL_ARTEM", "Happy moment. map zoom rendered!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
