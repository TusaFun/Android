package com.jupiter.tusa.map;

import android.graphics.Bitmap;

public interface OnTilePrepared {
    void received(Bitmap tile, float[] vertexLocations);
}
