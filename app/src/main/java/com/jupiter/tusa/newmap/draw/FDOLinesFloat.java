package com.jupiter.tusa.newmap.draw;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FDOLinesFloat implements DrawOpenGlProgram {
    private float[] vertexCoordinates;
    private int coordinatesPerVertex;
    private int vertexStride;
    private int sizeOfOneCoordinate = 4;
    private FloatBuffer vertexBuffer;
    private int mProgram;
    private float[] color;

    public FDOLinesFloat(
            float[] vertexCoordinates,
            int coordinatesPerVertex,
            int fragmentShaderPointer,
            int vertexShaderPointer,
            float[] color
    ) {
        this.vertexCoordinates = vertexCoordinates;
        this.coordinatesPerVertex = coordinatesPerVertex;
        this.color = color;
        vertexStride = coordinatesPerVertex * sizeOfOneCoordinate;

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoordinates.length * sizeOfOneCoordinate);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoordinates);
        vertexBuffer.position(0);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderPointer);
        GLES20.glAttachShader(mProgram, fragmentShaderPointer);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);
    }

    @Override
    public void draw(float[] mvpMatrix) {
        int handle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(handle, 1, false, mvpMatrix, 0);

        int vertexAttributePointerHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(vertexAttributePointerHandle);
        GLES20.glVertexAttribPointer(
                vertexAttributePointerHandle,
                coordinatesPerVertex,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
        );

        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glLineWidth(20f);
        int capacity = vertexBuffer.capacity();
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, capacity / coordinatesPerVertex);
        GLES20.glDisableVertexAttribArray(vertexAttributePointerHandle);
    }
}
