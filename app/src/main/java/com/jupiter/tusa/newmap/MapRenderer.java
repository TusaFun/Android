package com.jupiter.tusa.newmap;

import com.jupiter.tusa.R;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.newmap.gl.DrawFrame;
import com.jupiter.tusa.newmap.gl.ShadersBuilderAndStorage;
import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import com.jupiter.tusa.newmap.thread.result.handlers.RunnableHandler;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MapRenderer implements GLSurfaceView.Renderer {
    private MapWorldCamera mapWorldCamera;
    private final MainActivity mainActivity;
    private final RunnableHandler<MapRenderer> glSurfaceCreatedHandler;
    private final RunnableHandler<MapRenderer> glSurfaceChangedHandler;

    private final DrawFrame drawFrame;
    private final ShadersBuilderAndStorage shadersBuilderAndStorage;

    public DrawFrame getDrawFrame() {return drawFrame;}
    public MapWorldCamera getMapWorldCamera() {return mapWorldCamera;}

    public MapRenderer(
            MainActivity mainActivity,
            float initScaleFactor,
            RunnableHandler<MapRenderer> glSurfaceCreated,
            RunnableHandler<MapRenderer> glSurfaceChanged
    ) {
        this.mainActivity = mainActivity;
        this.shadersBuilderAndStorage = new ShadersBuilderAndStorage(mainActivity);
        this.glSurfaceCreatedHandler = glSurfaceCreated;
        this.glSurfaceChangedHandler = glSurfaceChanged;
        mapWorldCamera = new MapWorldCamera(
                0,
                0,
                initScaleFactor
        );
        drawFrame =  new DrawFrame(shadersBuilderAndStorage);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        float[] landColor = ColorUtils.hslaToRgba(60, 0, 100, 1);
        GLES20.glClearColor(landColor[0], landColor[1], landColor[2], landColor[3]);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        shadersBuilderAndStorage.saveAndBuild("basic", R.raw.basic_vertex_shader, R.raw.basic_fragment_shader);
        glSurfaceCreatedHandler.handle(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mapWorldCamera.updateViewportSize(width, height);
        glSurfaceChangedHandler.handle(this);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        float[] modelViewMatrix = mapWorldCamera.getModelViewMatrix();

        for (DrawOpenGlProgram drawMePlease : drawFrame.getDrawMePlease()) {
            drawMePlease.draw(modelViewMatrix);
        }
    }
}
