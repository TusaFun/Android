package com.jupiter.tusa.newmap.event;

import com.jupiter.tusa.newmap.MapRenderer;
import com.jupiter.tusa.newmap.MapSurfaceView;

public class GlSurfaceChangedEvent implements MapSignatureEvent<MapRenderer> {
    private final MapSurfaceView mapSurfaceView;
    public GlSurfaceChangedEvent(MapSurfaceView mapSurfaceView) {
        this.mapSurfaceView = mapSurfaceView;
    }
    @Override
    public void handle(MapRenderer input) {
        mapSurfaceView.getMapTilesShower().nextMapState();
    }
}
