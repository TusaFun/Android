package com.jupiter.tusa.newmap.camera;

import android.opengl.Matrix;

import androidx.core.math.MathUtils;

import com.jupiter.tusa.newmap.DistanceToTileZ;
import com.jupiter.tusa.newmap.MapMath;
import com.jupiter.tusa.newmap.TileWorldCoordinates;

public class MapWorldCamera {
    private float x;
    private float y;
    private float initScaleFactor;
    private float ratio;
    private int viewportHeight;
    private int viewportWidth;
    private float[] upperLeftWorldViewCoordinates;
    private float[] bottomRightWorldViewCoordinates;
    private TileWorldCoordinates tileWorldCoordinates;

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
            float initScaleFactor,
            TileWorldCoordinates tileWorldCoordinates
    ) {
        this.x = x;
        this.y = y;
        this.initScaleFactor = initScaleFactor;
        this.tileWorldCoordinates = tileWorldCoordinates;
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

    public ViewTitles getViewTiles() {
        int mapZ = tileWorldCoordinates.getCurrentZ();
        float extent = tileWorldCoordinates.getCurrentExtent();
        float startX = upperLeftWorldViewCoordinates[0];
        float endX = bottomRightWorldViewCoordinates[0];
        float startY = -1 * upperLeftWorldViewCoordinates[1];
        float endY = -1 * bottomRightWorldViewCoordinates[1];
        int borderAmountTiles = (int) Math.pow(2, mapZ);
        int maxTileNumber = borderAmountTiles - 1;

        int startXTile = (int) (startX / extent);
        startXTile = MathUtils.clamp(startXTile, 0, maxTileNumber);
        int endXTile = (int) (endX / extent);
        endXTile = MathUtils.clamp(endXTile, 0, maxTileNumber);
        int startYTile = (int) (startY / extent);
        startYTile = MathUtils.clamp(startYTile, 0, maxTileNumber);
        int endYTile = (int) (endY / extent);
        endYTile = MathUtils.clamp(endYTile, 0, maxTileNumber);
        return new ViewTitles(startXTile, endXTile, startYTile, endYTile, mapZ);
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
        float resultZoomK = (float) (Math.pow(initScaleFactor, DistanceToTileZ.zoomingParabolaPower));

        float fieldOfView = maxFieldOfView * resultZoomK;
        float eyeZ = maxEyeZ * resultZoomK;

        float viewZDelta = 2f;
        Matrix.setLookAtM(viewMatrix, 0, x, y, eyeZ, x, y, 0f, 0f, 1.0f, 0.0f);
        Matrix.perspectiveM(projectionMatrix, 0, fieldOfView, ratio, eyeZ - viewZDelta, eyeZ + viewZDelta);
        Matrix.multiplyMM(modelViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        updateVisibleCornersWorldCoordinates();
    }
}
