package com.jupiter.tusa.nativemap;


import android.content.Context;
import android.opengl.GLSurfaceView;

public class MapView extends GLSurfaceView {


    public MapView(Context context) {
        super(context);
        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());
        setRenderer(new com.jupiter.tusa.nativemap.Renderer(getResources().getAssets()));
    }
}