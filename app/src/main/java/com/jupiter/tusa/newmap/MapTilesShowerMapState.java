package com.jupiter.tusa.newmap;

import com.jupiter.tusa.newmap.gl.MapWorldCamera;

public class MapTilesShowerMapState {
    private int tileZ;
    private MapWorldCamera mapWorldCamera;

    public int getTileZ() {
        return tileZ;
    }
    public MapWorldCamera getMapWorldCamera() {return mapWorldCamera;}

    public MapTilesShowerMapState(int tileZ, MapWorldCamera mapWorldCamera) {
        this.tileZ = tileZ;
        this.mapWorldCamera = mapWorldCamera;
    }
}
