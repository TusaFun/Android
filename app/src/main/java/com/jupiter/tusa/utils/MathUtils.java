package com.jupiter.tusa.utils;

public class MathUtils {
    public static double calculateVectorLength(float x, float y) {
        return Math.sqrt(x * x + y * y);
    }
    public static float[] makePortionedLinePow2(float lineSize, int partsAmount) {
        int partSize = (int) (lineSize / partsAmount);
        float[] portioned = new float[partsAmount];
        for (int i = 0; i < partsAmount; i++) {
            int end = (i + 1) * partSize;
            portioned[i] = end;
        }
        return portioned;
    }
}
