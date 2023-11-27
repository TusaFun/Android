package com.jupiter.tusa.newmap.mvt;

import java.util.Map;
import vector_tile.VectorTile;

public class MvtObject {
    protected MvtShapes shape;
    protected float[] vertices;
    protected int[] drawOrder;
    protected String layerName;
    protected Map<String, VectorTile.Tile.Value> tags;
    protected int coordinatesPerVertex = 2;
    protected int sizeOfOneCoordinate = 4;

    public int getSizeOfOneCoordinate() {return sizeOfOneCoordinate;}
    public int getCoordinatesPerVertex() {return coordinatesPerVertex;}
    public String getLayerName() {return layerName;}
    public float[] getVertices() {return vertices;}
    public int[] getDrawOrder() {return drawOrder;}
    public MvtShapes getShape() {return shape;}
    public Map<String, VectorTile.Tile.Value> getTags() {return tags;}

    public MvtObject(
            float[] vertices,
            int[] drawOrder,
            String layerName,
            Map<String, VectorTile.Tile.Value> tags,
            MvtShapes shape
    ) {
        this.vertices = vertices;
        this.layerName = layerName;
        this.tags = tags;
        this.drawOrder = drawOrder;
        this.shape = shape;
    }

    public void translate(float dx, float dy, double multiply) {
        for (int i = 0; i < vertices.length / coordinatesPerVertex; i++) {
            int index = i * coordinatesPerVertex;
            vertices[index] = (float) (vertices[index] * multiply + dx);
            vertices[index + 1] = (float) (vertices[index + 1] * multiply + dy);
        }
    }
}
