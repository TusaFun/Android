package com.jupiter.tusa.newmap.mvt;

import java.util.Map;

import vector_tile.VectorTile;

public class MvtLines extends MvtObject {
    public MvtLines(
            float[] vertices,
            int[] drawOrder,
            String layerName,
            Map<String, VectorTile.Tile.Value> tags
    ) {
        super(vertices, drawOrder, layerName, tags);
    }
}
