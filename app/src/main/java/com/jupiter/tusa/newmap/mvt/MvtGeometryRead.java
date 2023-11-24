package com.jupiter.tusa.newmap.mvt;
import java.util.ArrayList;
import vector_tile.VectorTile;

public class MvtGeometryRead {
    public ArrayList<Vertices> vertices;
    public VectorTile.Tile.GeomType geomType;
    public MvtGeometryRead(ArrayList<Vertices> vertices, VectorTile.Tile.GeomType geomType) {
        this.vertices = vertices;
        this.geomType = geomType;
    }
}
