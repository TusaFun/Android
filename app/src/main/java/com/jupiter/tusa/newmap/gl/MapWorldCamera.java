package com.jupiter.tusa.newmap.gl;

import android.opengl.Matrix;
import com.jupiter.tusa.newmap.MapMath;

public class MapWorldCamera {
    private float x;
    private float y;
    private float initScaleFactor;
    private float ratio;
    private int viewportHeight;
    private int viewportWidth;
    private float[] upperLeftWorldViewCoordinates;
    private float[] bottomRightWorldViewCoordinates;

    private final float[] modelViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    public float[] getModelViewMatrix() {return modelViewMatrix; }
    public float[] getUpperLeftWorldViewCoordinates() {
        return upperLeftWorldViewCoordinates;
    }
    public float[] getBottomRightWorldViewCoordinates() {
        return bottomRightWorldViewCoordinates;
    }
    public float getGlWorldWidth() {
        return bottomRightWorldViewCoordinates[0] - upperLeftWorldViewCoordinates[0];
    }

    public MapWorldCamera(
            float x,
            float y,
            float initScaleFactor
    ) {
        this.x = x;
        this.y = y;
        this.initScaleFactor = initScaleFactor;
    }

    public void moveXY(float x, float y) {
        this.x = x;
        this.y = y;
        newPosition();
    }

    public void moveZ(float distancePortion) {
        this.initScaleFactor = distancePortion;
        newPosition();
    }

    public void updateViewportSize(int width, int height) {
        this.viewportHeight = height;
        this.viewportWidth = width;
        this.ratio = (float) width / height;
        newPosition();
    }

    private void updateVisibleCornersWorldCoordinates() {
        upperLeftWorldViewCoordinates = MapMath.screenCoordinatesToWorldLocation(
                viewportHeight, viewportWidth,0, 0, modelViewMatrix
        );
        bottomRightWorldViewCoordinates = MapMath.screenCoordinatesToWorldLocation(
                viewportHeight, viewportWidth,viewportWidth, viewportHeight, modelViewMatrix
        );
    }

    private void newPosition() {
        float maxFieldOfView = 60;
        float maxEyeZ = (float) Math.pow(2, 19);
        float k = 10;
        float multiply = (float) (Math.pow(initScaleFactor, 4));
        float resultZoomK = multiply;

        float fieldOfView = maxFieldOfView * resultZoomK;
        float eyeZ = maxEyeZ * resultZoomK;
        //Log.d("GL_ARTEM", String.format("Result zoom k = %.3f, portion = %.3f", resultZoomK, distancePortion));

        float viewZDelta = 10f;
        Matrix.setLookAtM(viewMatrix, 0, x, y, eyeZ, x, y, 0f, 0f, 1.0f, 0.0f);
        Matrix.perspectiveM(projectionMatrix, 0, fieldOfView, ratio, eyeZ - viewZDelta, eyeZ + viewZDelta);
        Matrix.multiplyMM(modelViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        updateVisibleCornersWorldCoordinates();
    }
}
