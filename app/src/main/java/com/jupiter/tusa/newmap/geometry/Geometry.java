package com.jupiter.tusa.newmap.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Geometry {
    private int coordinatesPerVertex = 3;
    private float vertexCoordinates[];
    private int vertexCount;
    private int vertexStride;
    private int[] drawOrder;
    private FloatBuffer vertexBuffer;
    private IntBuffer drawListBuffer;

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public int[] getDrawOrder() {
        return drawOrder;
    }

    public IntBuffer getDrawListBuffer() {
        return drawListBuffer;
    }

    public int getCoordinatesPerVertex() {
        return coordinatesPerVertex;
    }

    public int getVertexStride() {
        return vertexStride;
    }

    public Geometry(float[] vertexCoordinates, int coordinatesPerVertex, int[] drawOrder) {
        this.coordinatesPerVertex = coordinatesPerVertex;
        this.vertexCoordinates = vertexCoordinates;
        this.drawOrder = drawOrder;
        vertexCount = vertexCoordinates.length / coordinatesPerVertex;
        vertexStride = coordinatesPerVertex * 4;

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoordinates.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoordinates);
        vertexBuffer.position(0);

        if(drawOrder != null) {
            ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 4);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asIntBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);
        }
    }

    public Geometry move(float x, float y) {
        for(int i = 0; i < vertexCoordinates.length; i += coordinatesPerVertex) {
            vertexCoordinates[i] += x;
            vertexCoordinates[i+1] += y;
        }

        return new Geometry(vertexCoordinates, coordinatesPerVertex, drawOrder);
    }
}
