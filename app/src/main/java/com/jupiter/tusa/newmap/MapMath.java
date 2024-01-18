package com.jupiter.tusa.newmap;

import android.opengl.Matrix;

public class MapMath {
    public static float[] screenCoordinatesToWorldLocation(
            int viewportWidth, int viewportHeight,
            float x, float y,
            float[] modelViewMatrix
    ) {
        float nx = x * 2 / viewportWidth - 1;
        float ny = 1 - (y * 2 / viewportHeight);
        float nz = 0f;

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
}
