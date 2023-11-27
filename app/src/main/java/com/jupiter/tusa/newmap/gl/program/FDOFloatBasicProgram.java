package com.jupiter.tusa.newmap.gl.program;

import android.opengl.GLES20;
import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class FDOFloatBasicProgram implements DrawOpenGlProgram {
    private List<FDOFloatBasicInput> input;
    private int mProgram;

    public FDOFloatBasicProgram(
            List<FDOFloatBasicInput> input,
            int fragmentShaderPointer,
            int vertexShaderPointer
    ) {
        this.input = input;

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

        for(FDOFloatBasicInput input : input) {
            int coordinatesPerVertex = input.getCoordinatesPerVertex();
            int vertexStride = input.getVertexStride();
            int drawOrderLength = input.getDrawOrderLength();
            FloatBuffer vertexBuffer = input.getVertexBuffer();
            IntBuffer drawListBuffer = input.getDrawListBuffer();
            float[] color = input.getColor();

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
            GLES20.glLineWidth(input.getLineWidth());
            int mode = input.getDrawMode();
            GLES20.glDrawElements(mode, drawOrderLength, GLES20.GL_UNSIGNED_INT, drawListBuffer);

            GLES20.glDisableVertexAttribArray(vertexAttributePointerHandle);
        }
    }
}