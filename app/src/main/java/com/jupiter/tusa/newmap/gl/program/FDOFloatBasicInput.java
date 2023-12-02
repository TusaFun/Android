package com.jupiter.tusa.newmap.gl.program;

import android.opengl.GLES20;

import com.jupiter.tusa.newmap.mvt.MvtShapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FDOFloatBasicInput {
    private final int key;
    private final int vertexStride;
    private final int coordinatesPerVertex;
    private final int drawOrderLength;
    private final FloatBuffer vertexBuffer;
    private final IntBuffer drawListBuffer;
    private final float[] color;
    private final int drawMode;
    private final float lineWidth;

    public int getKey() { return key; }
    public float getLineWidth() {return lineWidth;}
    public int getDrawMode() {return drawMode;}
    public int getDrawOrderLength() {return drawOrderLength;}
    public int getCoordinatesPerVertex() {return coordinatesPerVertex;}
    public int getVertexStride() {return vertexStride;}
    public FloatBuffer getVertexBuffer() {return vertexBuffer;}
    public IntBuffer getDrawListBuffer() {return drawListBuffer;}
    public float[] getColor() {return color;}

    public FDOFloatBasicInput(
            int key,
            float[] vertexCoordinates,
            int[] drawOrder,
            int coordinatesPerVertex,
            int sizeOfOneCoordinate,
            int drawMode,
            float[] color,
            float lineWidth
    ) {
        this.key = key;
        this.lineWidth = lineWidth;
        this.drawMode = drawMode;
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

    public static int getModeFromMvtShape(MvtShapes shape) {
        if(shape == MvtShapes.LINE) {
            return GLES20.GL_LINES;
        }
        if(shape == MvtShapes.POLYGON) {
            return GLES20.GL_TRIANGLES;
        }

        return -1;
    }
}
