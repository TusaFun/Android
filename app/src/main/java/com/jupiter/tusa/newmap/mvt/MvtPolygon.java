package com.jupiter.tusa.newmap.draw;

public class TrianglesDrawInput {
    public float[] vertices;
    public int[] triangles;
    public int dimension;

    public TrianglesDrawInput(float[] vertices, int[] triangles, int dimension) {
        this.triangles = triangles;
        this.vertices = vertices;
        this.dimension = dimension;
    }
}
