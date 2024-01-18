package com.jupiter.tusa.nativemap;

import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;

import com.jupiter.tusa.NativeLibrary;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private final AssetManager _assetManager;

    public Renderer(AssetManager assetManager) {
        _assetManager = assetManager;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        NativeLibrary.surfaceCreated(_assetManager);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        NativeLibrary.init(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        NativeLibrary.step();
    }
}
