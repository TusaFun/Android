package com.jupiter.tusa.newmap;

import com.jupiter.tusa.newmap.mvt.MvtObject;

public class TileWorldCoordinates {
    public float getTileZExtent(int z) {
        int multiply = (int) Math.pow(2, 19 - z);
        return (float) multiply;
    }

    public void applyToTileMvt(MvtObject mvtObject, int x, int y, int z) {
        int extentPow = 19 - z;
        int extent = (int) Math.pow(2, extentPow);
        float worldX = x * extent;
        float worldY = - y * extent;
        double multiply = Math.pow(2, extentPow - 12);
        mvtObject.translate(worldX, worldY, multiply);
    }
}
