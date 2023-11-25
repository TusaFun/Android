package com.jupiter.tusa.utils;

import java.util.List;

public class ArrayUtils {
    public static float[] ToArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static int[] ToArrayInt(List<Integer> list) {
        int[] arr = new int[list.size()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static double[] floatToDouble(List<Float> list) {
        double[] arr = new double[list.size()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static int[][][] combineArrays(int[][][] array1, int[][][] array2) {
        int[][][] combinedArray = new int[array1.length + array2.length][array1[0].length + array2[0].length][];

        for(int row = 0; row < array1.length; row++) {
            for(int cols = 0; cols < array1[0].length; cols++) {
                combinedArray[row][cols] = array1[row][cols];
            }
        }

        for(int row = 0; row < array2.length; row++) {
            for(int cols = 0; cols < array2[0].length; cols++) {
                combinedArray[row + array1.length][cols + array1[0].length] = array2[row][cols];
            }
        }

        return combinedArray;
    }
}
