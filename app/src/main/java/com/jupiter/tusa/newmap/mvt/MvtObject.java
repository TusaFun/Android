package com.jupiter.tusa.newmap.mvt;

import java.util.Map;

import vector_tile.VectorTile;

public class MvtObject {
    protected float[] vertices;
    protected String layerName;
    protected Map<String, VectorTile.Tile.Value> tags;

    public String getLayerName() {return layerName;}
    public float[] getVertices() {return vertices;}

    public MvtObject(float[] vertices, String layerName, Map<String, VectorTile.Tile.Value> tags) {
        this.vertices = vertices;
        this.layerName = layerName;
        this.tags = tags;
    }

    public void translate(float dx, float dy, double multiply) {
        for (int i = 0; i < vertices.length / 2; i++) {
            int index = i * 2;
            vertices[index] = (float) (vertices[index] * multiply + dx);
            vertices[index + 1] = (float) (vertices[index + 1] * multiply + dy);
        }
    }
}
