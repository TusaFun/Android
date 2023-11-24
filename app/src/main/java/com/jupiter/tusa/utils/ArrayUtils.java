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
}
