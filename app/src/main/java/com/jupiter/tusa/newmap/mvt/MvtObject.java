package com.jupiter.tusa.newmap.mvt;

import android.util.Log;

import com.jupiter.tusa.utils.ArrayUtils;

import java.util.Map;

import vector_tile.VectorTile;

public class MvtObject {
    protected float[] vertices;
    protected String layerName;
    protected Map<String, VectorTile.Tile.Value> tags;
    private int coordinatesPerVertex = 2;
    private int sizeOfOneCoordinate = 4;

    public int getSizeOfOneCoordinate() {return sizeOfOneCoordinate;}
    public int getCoordinatesPerVertex() {return coordinatesPerVertex;}
    public String getLayerName() {return layerName;}
    public float[] getVertices() {return vertices;}
    public Map<String, VectorTile.Tile.Value> getTags() {return tags;}

    public MvtObject(float[] vertices, String layerName, Map<String, VectorTile.Tile.Value> tags) {
        this.vertices = vertices;
        this.layerName = layerName;
        this.tags = tags;
    }

    public void translate(float dx, float dy, double multiply) {
        for (int i = 0; i < vertices.length / coordinatesPerVertex; i++) {
            int index = i * coordinatesPerVertex;
            vertices[index] = (float) (vertices[index] * multiply + dx);
            vertices[index + 1] = (float) (vertices[index + 1] * multiply + dy);
        }
    }

    public void insertZCoordinate(float z) {
        if(coordinatesPerVertex == 3) {
            Log.e("GL_ARTEM", "Координата z уже была вставлена.");
            return;
        }
        coordinatesPerVertex = 3;
        float[] newVertices = new float[vertices.length / 2 + vertices.length];
        for(int i = 0; i < newVertices.length; i+= 3) {
            int indexFor2d = i / 3 * 2;
            newVertices[i] = vertices[indexFor2d];
            newVertices[i + 1] = vertices[indexFor2d + 1];
            newVertices[i + 2] = z;
        }
        vertices = newVertices;
    }
}
