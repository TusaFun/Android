package com.jupiter.tusa.newmap.gl.program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FDOFloatBasicInput {
    private int vertexStride;
    private int coordinatesPerVertex;
    private int drawOrderLength;
    private FloatBuffer vertexBuffer;
    private IntBuffer drawListBuffer;
    private float[] color;
    private short drawType;
    private float lineWidth;

    public float getLineWidth() {return lineWidth;}
    public short getDrawType() {return drawType;}
    public int getDrawOrderLength() {return drawOrderLength;}
    public int getCoordinatesPerVertex() {return coordinatesPerVertex;}
    public int getVertexStride() {return vertexStride;}
    public FloatBuffer getVertexBuffer() {return vertexBuffer;}
    public IntBuffer getDrawListBuffer() {return drawListBuffer;}
    public float[] getColor() {return color;}

    public FDOFloatBasicInput(
            float[] vertexCoordinates,
            int[] drawOrder,
            int coordinatesPerVertex,
            int sizeOfOneCoordinate,
            short drawType,
            float[] color,
            float lineWidth
    ) {
        this.lineWidth = lineWidth;
        this.drawType = drawType;
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
