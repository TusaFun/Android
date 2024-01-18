package com.jupiter.tusa.newmap;

import com.jupiter.tusa.newmap.mvt.MvtObject;

public class TileWorldCoordinates {
    private int currentZ;
    private int currentTileX = 0;
    private int currentTileY = 0;
    private float currentExtent;

    public int getCurrentZ() {
        return currentZ;
    }

    public float getCurrentExtent() {
        return currentExtent;
    }

    public void updateMapZ(int z) {
        currentZ = z;
        currentExtent = (float) Math.pow(2, 19 - z);
    }

    public boolean shouldUpdateStateOnMove(float x, float y) {
        int newX = (int) (x * 2 / currentExtent);
        int newY = (int) (y * 2 / currentExtent);

        if(currentTileX != newX || currentTileY != newY) {
            currentTileX = newX;
            currentTileY = newY;
            return true;
        }
        return false;
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
