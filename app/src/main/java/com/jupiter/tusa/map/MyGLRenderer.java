package com.jupiter.tusa.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.service.quicksettings.Tile;
import android.util.Log;

import com.jupiter.tusa.MainActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private MainActivity mainActivity;
    private MyGlSurfaceView myGlSurfaceView;

    // viewport
    private float ratio;
    private int width;
    private int height;

    // Камера
    private float fov = 45f;

    // Карта
    private Sprite[] tiles = new Sprite[64];

    private final float[] modelViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    public float getFov() {
        return fov;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRatio() {
        return ratio;
    }

    public MyGLRenderer(Context context, MyGlSurfaceView myGlSurfaceView) {
        this.context = context;
        this.mainActivity = (MainActivity) context;
        this.myGlSurfaceView = myGlSurfaceView;
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public float[] screenCoordinatesToWorldLocation(float x, float y) {
        float nx = x * 2 / width - 1;
        float ny = 1 - (y * 2 / height);
        float nz = -1f;

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

    public void renderTile(Bitmap tile, float[] vertexLocations, int index, int totalTilesWaitForLoad) {
        Sprite sprite = new Sprite(mainActivity, tile, index, vertexLocations);
        tiles[index] = sprite;
        for(int i = totalTilesWaitForLoad; i < tiles.length; i++) {
            tiles[i] = null;
        }
    }

    public void setFov(float fov) {
        this.fov = fov;
        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 6, 7);
    }

    public void moveCameraHorizontally(float x, float y) {
        Matrix.setLookAtM(viewMatrix, 0, x, y, 6f, x, y, 0f, 0f, 1.0f, 0.0f);
    }

    public WorldBounds calcWorldLocationViewportBounds() {
        float topLeftViewportX = 0f;
        float topLeftViewportY = 0f;

        float bottomRightX = width;
        float bottomRightY = height;

        float[] topLeftWorld = screenCoordinatesToWorldLocation(topLeftViewportX, topLeftViewportY);
        float topLeftXWorld = topLeftWorld[0];
        float topLeftYWorld = topLeftWorld[1];
        float topLeftZWorld = topLeftWorld[2];

        float[] bottomRightWorld = screenCoordinatesToWorldLocation(bottomRightX, bottomRightY);
        float bottomRightXWorld = bottomRightWorld[0];
        float bottomRightYWorld = bottomRightWorld[1];
        float bottomRightZWorld = bottomRightWorld[2];

        return new WorldBounds(topLeftXWorld, topLeftYWorld, bottomRightXWorld, bottomRightYWorld);
    }

    public LatLonBounds calcLatLonBounds(WorldBounds worldBounds) {
        float topLeftXWorld = worldBounds.topLeftXWorld;
        float topLeftYWorld = worldBounds.topLeftYWorld;

        float bottomRightXWorld = worldBounds.bottomRightXWorld;
        float bottomRightYWorld = worldBounds.bottomRightYWorld;

        float topLeftLatitude = TileCoordinateConverter.worldYToLatitude(topLeftYWorld);
        float topLeftLongitude = TileCoordinateConverter.worldXToLongitude(topLeftXWorld);

        float bottomRightLatitude = TileCoordinateConverter.worldYToLatitude(bottomRightYWorld);
        float bottomRightLongitude = TileCoordinateConverter.worldXToLongitude(bottomRightXWorld);

        return new LatLonBounds(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude);
        //Log.d("GL_ARTEM", String.format("TopLeft lat = %.3f lon = %.3f BottomRight lat = %.3f lon = %.3f", topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude));
    }

    public TilesBounds calcTilesBounds(LatLonBounds bounds, int z) {
        int[] topLeftTileCoordinates = TileCoordinateConverter.convertToTileCoordinates(bounds.getTopLeftLatitude(), bounds.getTopLeftLongitude(), z);
        int[] rightBottomTileCoordinates = TileCoordinateConverter.convertToTileCoordinates(bounds.getBottomRightLatitude(), bounds.getBottomRightLongitude(), z);

        return new TilesBounds(topLeftTileCoordinates[0], topLeftTileCoordinates[1], rightBottomTileCoordinates[0], rightBottomTileCoordinates[1]);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(modelViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        for(int i = 0; i < tiles.length; i++) {
            Sprite tile = tiles[i];
            if(tile != null) {
                tile.draw(modelViewMatrix);
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0.95f, 0.95f, 1.0f, 1.0f);

        myGlSurfaceView.renderMap(myGlSurfaceView.getMapZ());
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.height = height;
        this.width = width;
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 6f, 0, 0f, 0f, 0f, 1.0f, 0.0f);

        ratio = (float) width / height;
        float fov = 45;
        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 6, 7);
    }
}
