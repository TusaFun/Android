package com.jupiter.tusa.newmap.mvt;

import java.util.Map;

import vector_tile.VectorTile;

public class MvtPolygon extends MvtObject{
    public int[] triangles;

    public MvtPolygon(
            float[] vertices,
            int[] triangles,
            String layerName,
            Map<String, VectorTile.Tile.Value> tags
    ) {
        super(vertices, layerName, tags);
        this.triangles = triangles;
    }
}
