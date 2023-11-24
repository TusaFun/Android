package com.jupiter.tusa.newmap.mvt;

import java.util.Map;

import vector_tile.VectorTile;

public class MvtLines extends MvtObject {
    public MvtLines(
            float[] vertices,
            String layerName,
            Map<String, VectorTile.Tile.Value> tags
    ) {
        super(vertices, layerName, tags);
    }
}
