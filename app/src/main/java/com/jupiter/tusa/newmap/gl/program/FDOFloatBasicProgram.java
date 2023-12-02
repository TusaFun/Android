package com.jupiter.tusa.newmap.gl.program;

import android.opengl.GLES20;
import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;

public class FDOFloatBasicProgram implements DrawOpenGlProgram {
    private final List<FDOFloatBasicInput> input;
    private final int mProgram;

    public FDOFloatBasicProgram(
            List<FDOFloatBasicInput> initInput,
            int fragmentShaderPointer,
            int vertexShaderPointer
    ) {
        this.input = initInput;

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderPointer);
        GLES20.glAttachShader(mProgram, fragmentShaderPointer);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);
    }

    public void addDataForDrawing(List<FDOFloatBasicInput> fdoFloatBasicInput) {
        synchronized (input) {
            input.addAll(fdoFloatBasicInput);
        }
    }

    public void clearIrrelevant(int freshKey) {
        synchronized (input) {
            Iterator<FDOFloatBasicInput> iterator = input.iterator();
            while (iterator.hasNext()) {
                FDOFloatBasicInput data = iterator.next();
                if(data.getKey() < freshKey) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void draw(float[] mvpMatrix) {
        int handle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(handle, 1, false, mvpMatrix, 0);
        synchronized (input)
        {
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
}