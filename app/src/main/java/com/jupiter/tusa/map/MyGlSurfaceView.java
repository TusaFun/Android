package com.jupiter.tusa.map;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.map.scale.MapScaleListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MyGlSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private MainActivity mainActivity;

    // Маштабирование карты
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;

    // Перемещение карты
    private float previousX = 0f;
    private float previousY = 0f;
    private float currentMapX = 0f;
    private float currentMapY = 0f;
    private float moveStrong = 500f;

    public MyGlSurfaceView(Context context, AttributeSet attributeSet) throws ExecutionException, InterruptedException, TimeoutException {
        super(context, attributeSet);
        mainActivity = (MainActivity) context;
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context, this);
        setRenderer(renderer);

        mapScaleListener = new MapScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        if(event.getPointerCount() == 2) {
            // Маштабирование карты
            float scaleFactor = mapScaleListener.getScaleFactor();
            //Log.d("GL_ARTEM", "Scale factor = " + scaleFactor);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            // Перемещение по карте
            float dx = previousX - x;
            float dy = previousY - y;

            currentMapX += dx / moveStrong;
            currentMapY -= dy / moveStrong;
            Log.d("GL_ARTEM", String.format("dx = %.3f dy = %.3f mapX = %.3f mapY = %.3f", dx, dy, currentMapX, currentMapY));
            renderer.moveCameraHorizontally(currentMapX, currentMapY);
        }

        previousX = x;
        previousY = y;

        requestRender();
        return true;
    }

    private float getDistance(MotionEvent event) {
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }


    private float xToLongitude(float worldX) {
        float longitudeRange = 180.0f;
        return worldX * longitudeRange;
    }

    private float yToLatitude(float worldY) {
        float longitudeRange = 90.0f;
        return worldY * longitudeRange;
    }
}
