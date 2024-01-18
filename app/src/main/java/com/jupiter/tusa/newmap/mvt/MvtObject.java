package com.jupiter.tusa.newmap.mvt;

import java.util.List;
import java.util.Map;
import vector_tile.VectorTile;

public class MvtObject {
    protected MvtShapes shape;
    protected List<Float> vertices;
    protected List<Integer> drawOrder;
    protected String layerName;
    protected Map<String, VectorTile.Tile.Value> tags;
    protected int coordinatesPerVertex = 2;
    protected int sizeOfOneCoordinate = 4;

    public int getSizeOfOneCoordinate() {return sizeOfOneCoordinate;}
    public int getCoordinatesPerVertex() {return coordinatesPerVertex;}
    public String getLayerName() {return layerName;}
    public List<Float> getVertices() {return vertices;}
    public List<Integer> getDrawOrder() {return drawOrder;}
    public MvtShapes getShape() {return shape;}
    public Map<String, VectorTile.Tile.Value> getTags() {return tags;}

    public MvtObject(
            List<Float> vertices,
            List<Integer> drawOrder,
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
        for (int i = 0; i < vertices.size() / coordinatesPerVertex; i++) {
            int index = i * coordinatesPerVertex;
            vertices.set(index, (float) (vertices.get(index) * multiply + dx));
            vertices.set(index + 1, (float) (vertices.get(index + 1) * multiply + dy));
        }
    }
}
