package com.jupiter.tusa.newmap;

public class DistanceToTileZ {
    private final int maxTileZoom;
    private final float[] portions = {
            1f, // zoom 0 верхняя граница
            0.9f, // zoom 2
            0.8f, // zoom 4
            0.7f, // zoom 6
            0.6f, // zoom 8
            0.5f, // zoom 10
            0.4f, // zoom 12
            0.3f, // zoom 14
            0.2f, // zoom 16
            0.1f, // zoom 18
    };

    public DistanceToTileZ(int maxTileZoom) {
        this.maxTileZoom = maxTileZoom;
    }

    public int calcCurrentTileZ(float scaleFactorNormalized) {
        //Log.d("GL_ARTEM", "zoom potion = " + current);
        for(int i = portions.length - 1; i >= 0; i--) {
            if(scaleFactorNormalized <= portions[i]) {
                return i * 2;
            }
        }
        return maxTileZoom;
    }

    public float calcDistanceForZ(int z) {
        return portions[z / 2];
    }
}
