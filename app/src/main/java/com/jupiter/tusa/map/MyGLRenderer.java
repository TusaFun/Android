package com.jupiter.tusa.map;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import com.jupiter.tusa.MainActivity;


import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private MainActivity mainActivity;
    private MyGlSurfaceView myGlSurfaceView;

    private float ratio;
    private int width;
    private int height;

    private List<Sprite> tiles = new ArrayList<>();

    private final float[] modelViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRatio() {
        return ratio;
    }

    public MyGLRenderer(Context context, MyGlSurfaceView myGlSurfaceView) {
        this.context = context;
        this.mainActivity = (MainActivity) context;
        this.myGlSurfaceView = myGlSurfaceView;
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public float[] screenCoordinatesToWorldLocation(float x, float y) {
        float nx = x * 2 / width - 1;
        float ny = 1 - (y * 2 / height);
        float nz = -1f;

        float[] invertedModelViewMatrix = new float[16];
        Matrix.invertM(invertedModelViewMatrix, 0, modelViewMatrix, 0);

        float[] worldCoordinates = new float[4];
        Matrix.multiplyMV(worldCoordinates, 0, invertedModelViewMatrix, 0, new float[] {nx, ny, nz, 1}, 0);
        float w = worldCoordinates[3];
        worldCoordinates[0] /= w;
        worldCoordinates[1] /= w;
        worldCoordinates[2] /= w;

        return worldCoordinates;
    }

    public void setFov(float fov) {
        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 6, 7);
    }

    public void moveCameraHorizontally(float x, float y) {
        Matrix.setLookAtM(viewMatrix, 0, x, y, 6f, x, y, 0f, 0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        Matrix.multiplyMM(modelViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        for(int i = 0; i < tiles.size(); i++) {
            Sprite tile = tiles.get(i);
            tile.draw(modelViewMatrix);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0.95f, 0.95f, 1.0f, 1.0f);
        Sprite firstTile = new Sprite(mainActivity, null, 0, new float[] {
                -1f, 1f,
                -1f, -1f,
                1f, -1f,
                1f, 1f
        }, 0f, 0f);
        tiles.add(firstTile);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.height = height;
        this.width = width;
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 6f, 0, 0f, 0f, 0f, 1.0f, 0.0f);

        ratio = (float) width / height;
        float fov = 45;
        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 6, 7);
    }
}
