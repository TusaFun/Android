package com.jupiter.tusa.newmap.thread.result.handlers;

import com.jupiter.tusa.newmap.MapRenderer;
import com.jupiter.tusa.newmap.MapSurfaceView;

public class GlSurfaceCreatedHandler implements RunnableHandler<MapRenderer> {
    private final MapSurfaceView mapSurfaceView;
    public GlSurfaceCreatedHandler(MapSurfaceView mapSurfaceView) {
        this.mapSurfaceView = mapSurfaceView;
    }

    @Override
    public void handle(MapRenderer input) {

    }
}
