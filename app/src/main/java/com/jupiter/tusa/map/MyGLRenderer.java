package com.jupiter.tusa.map;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import com.jupiter.tusa.MainActivity;


import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private MainActivity mainActivity;
    private MyGlSurfaceView myGlSurfaceView;
    private float ratio;

    private int width;
    private int height;

    private Sprite sprite;

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


    private final float[] modelViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    public float[] getViewMatrix() {
        return viewMatrix;
    }

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public float[] getModelViewMatrix() {
        return modelViewMatrix;
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public float[] screenCoordinatesToWorldLocation(float viewportX, float viewportY) {
        int[] viewport = {0,0,width,height};
        float[] world = new float[4];
        float realY = height - viewportY;
        GLU.gluUnProject(0, 0, 1f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, world, 0);

        return world;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 6f, 0, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(modelViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        sprite.draw(modelViewMatrix);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0.95f, 0.95f, 1.0f, 1.0f);
        sprite = new Sprite(mainActivity, null, 0, new float[] {
                -1f, 1f,
                -1f, -1f,
                1f, -1f,
                1f, 1f
        }, 0f, 0f);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.height = height;
        this.width = width;
        ratio = (float) width / height;
        float fov = 45;
        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 6, 7);
        //Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, 1, 7);
    }
}
