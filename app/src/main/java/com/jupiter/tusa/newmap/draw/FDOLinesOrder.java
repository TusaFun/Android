package com.jupiter.tusa.newmap.draw;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class FDOLinesOrder implements DrawOpenGlProgram {
    private int[] vertexCoordinates;
    private int coordinatesPerVertex;
    private int[] drawOrder;
    private int vertexStride;
    private int sizeOfOneCoordinate = 4;
    private IntBuffer vertexBuffer;
    private IntBuffer drawListBuffer;
    private int mProgram;

    public FDOLinesOrder(
            int[] vertexCoordinates,
            int coordinatesPerVertex,
            int[] drawOrder,
            String fragmentShaderCode,
            String vertexShaderCode
    ) {
        this.vertexCoordinates = vertexCoordinates;
        this.coordinatesPerVertex = coordinatesPerVertex;
        this.drawOrder = drawOrder;
        vertexStride = coordinatesPerVertex * sizeOfOneCoordinate;

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoordinates.length * sizeOfOneCoordinate);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asIntBuffer();
        vertexBuffer.put(vertexCoordinates);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * sizeOfOneCoordinate);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asIntBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = LoadShader.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = LoadShader.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
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
                GLES20.GL_INT,
                false,
                vertexStride,
                vertexBuffer
        );

        float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glLineWidth(20f);
        GLES20.glDrawElements(GLES20.GL_LINES, drawOrder.length, GLES20.GL_UNSIGNED_INT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(vertexAttributePointerHandle);
    }
}
