package com.jupiter.tusa.newmap.mvt;

import java.util.Map;

import vector_tile.VectorTile;

public class MvtPolygons extends MvtObject{
    public MvtPolygons(
            float[] vertices,
            int[] triangles,
            String layerName,
            Map<String, VectorTile.Tile.Value> tags
    ) {
        super(vertices, triangles, layerName, tags);
    }
}
