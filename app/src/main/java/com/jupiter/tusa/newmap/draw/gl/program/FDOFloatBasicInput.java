package com.jupiter.tusa.newmap.draw.gl.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FDOFloatTrianglesInput {
    private int vertexStride;
    private int coordinatesPerVertex;
    private int drawOrderLength;
    private FloatBuffer vertexBuffer;
    private IntBuffer drawListBuffer;
    private float[] color;

    public int getDrawOrderLength() {return drawOrderLength;}
    public int getCoordinatesPerVertex() {return coordinatesPerVertex;}
    public int getVertexStride() {return vertexStride;}
    public FloatBuffer getVertexBuffer() {return vertexBuffer;}
    public IntBuffer getDrawListBuffer() {return drawListBuffer;}
    public float[] getColor() {return color;}

    public FDOFloatTrianglesInput(
            float[] vertexCoordinates,
            int[] drawOrder,
            int coordinatesPerVertex,
            int sizeOfOneCoordinate,
            float[] color
    ) {
        this.color = color;
        this.coordinatesPerVertex = coordinatesPerVertex;
        drawOrderLength = drawOrder.length;
        vertexStride = coordinatesPerVertex * sizeOfOneCoordinate;

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoordinates.length * sizeOfOneCoordinate);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoordinates);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * sizeOfOneCoordinate);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asIntBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }
}
