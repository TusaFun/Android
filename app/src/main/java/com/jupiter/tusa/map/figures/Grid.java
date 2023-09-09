package com.jupiter.tusa.map.figures;

import android.opengl.GLES20;

import com.jupiter.tusa.map.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Grid {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private int vPMatrixHandle;

    private int positionHandle;
    private int colorHandle;

    private float Coords[];

    private final int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private final int mProgram;
    private FloatBuffer vertexBuffer;
    static final int COORDS_PER_VERTEX = 3;

    float color[] = { 0.2f, 0.2f, 0.2f, 1.0f };

    public Grid() {
        int size = (int) (2 / 0.05);
        Coords = new float[size * COORDS_PER_VERTEX * 2];
        int currentVertexIndex = 0;
        for(float i = -1; i <= 1; i += 0.05) {
            Coords[currentVertexIndex] = -0.5f;
            Coords[currentVertexIndex + 1] = i;
            Coords[currentVertexIndex + 2] = 0f;
            Coords[currentVertexIndex + 3] = 0.5f;
            Coords[currentVertexIndex + 4] = i;
            Coords[currentVertexIndex + 5] = 0f;
            currentVertexIndex += 6;
        }
        vertexCount = Coords.length / COORDS_PER_VERTEX;

        ByteBuffer bb = ByteBuffer.allocateDirect(Coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(Coords);
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);


        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }



    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

        // pass vertex coordinates
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // pass face color
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // draw
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
