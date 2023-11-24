package com.jupiter.tusa.newmap.thread.result.handlers;

import com.jupiter.tusa.newmap.MapRenderer;
import com.jupiter.tusa.newmap.MapSurfaceView;

public class GlSurfaceChangedHandler implements RunnableHandler<MapRenderer> {
    private final MapSurfaceView mapSurfaceView;
    public GlSurfaceChangedHandler(MapSurfaceView mapSurfaceView) {
        this.mapSurfaceView = mapSurfaceView;
    }
    @Override
    public void handle(MapRenderer input) {
        mapSurfaceView.sendCurrentMapStateToShower();
    }
}
