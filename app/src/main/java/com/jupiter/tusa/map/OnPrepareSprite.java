package com.jupiter.tusa.map;

import android.graphics.Bitmap;

public interface OnPrepareSprite {
    void ready(
            Bitmap bitmap,
            float[] vertexLocations,
            int useIndex,
            RenderTileInitiator initiator,
            Tile tile
    );
}
