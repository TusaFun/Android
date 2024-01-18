package com.jupiter.tusa;

import android.content.res.AssetManager;

public class NativeLibrary {
    static {
        System.loadLibrary("tusa");
    }
    public static native void init(int width, int height);
    public static native void step();
    public static native void surfaceCreated(AssetManager assetManager);
}
