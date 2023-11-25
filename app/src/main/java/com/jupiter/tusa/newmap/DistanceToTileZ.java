package com.jupiter.tusa.newmap;

public class DistanceToTileZ {
    public static float zoomingParabolaPower = 5;
    private final int maxTileZoom;
    private final float[] portions = {
            1f, // zoom 0 верхняя граница
            0.95f, // zoom 1
            0.9f, // zoom 2
            0.85f, // zoom 3
            0.8f, // zoom 4
            0.75f, // zoom 5
            0.7f, // zoom 6
            0.65f, // zoom 7
            0.6f, // zoom 8
            0.55f, // zoom 9
            0.5f, // zoom 10
            0.45f, // zoom 11
            0.40f, // zoom 12
            0.35f, // zoom 13
    };

    public DistanceToTileZ(int maxTileZoom) {
        this.maxTileZoom = maxTileZoom;
    }

    public int calcCurrentTileZ(float scaleFactorNormalized) {
        //Log.d("GL_ARTEM", "zoom potion = " + current);
        for(int i = portions.length - 1; i >= 0; i--) {
            if(scaleFactorNormalized <= portions[i]) {
                return i;
            }
        }
        return maxTileZoom;
    }

    public float calcDistanceForZ(int z) {
        return portions[z];
    }
}
