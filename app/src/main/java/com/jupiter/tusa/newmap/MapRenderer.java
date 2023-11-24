package com.jupiter.tusa.newmap;

import java.util.ArrayList;
import java.util.List;
import com.jupiter.tusa.R;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.newmap.gl.MapWorldCamera;
import com.jupiter.tusa.newmap.gl.DrawFrame;
import com.jupiter.tusa.newmap.gl.ShadersBuilderAndStorage;
import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicInput;
import com.jupiter.tusa.newmap.mvt.MvtLines;
import com.jupiter.tusa.newmap.mvt.MvtObject;
import com.jupiter.tusa.newmap.mvt.MvtObjectStyled;
import com.jupiter.tusa.newmap.mvt.MvtPolygon;
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
        GLES20.glClearColor(0.95f, 0.95f, 1.0f, 1.0f);
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
