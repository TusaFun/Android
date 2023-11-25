package com.jupiter.tusa.newmap;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.newmap.draw.MapStyle;
import com.jupiter.tusa.newmap.thread.result.handlers.GlSurfaceChangedHandler;
import com.jupiter.tusa.newmap.thread.result.handlers.GlSurfaceCreatedHandler;

public class MapSurfaceView extends GLSurfaceView {
    public MapSurfaceView(Context context) {
        super(context);
    }

    private MainActivity mainActivity;
    private MapRenderer mapRenderer;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private MapScaleListener mapScaleListener;
    private MapTilesShower mapTilesShower;
    private DistanceToTileZ distanceToTileZ;

    private float moveSpeed = 0.001f;
    private float currentMapX;
    private float currentMapY;
    private float previousScreenX;
    private float previousScreenY;
    private final MapStyle mapStyle = new MapStyle();

    private int maxTileZoom = 19;
    private int currentTileZ;

    public int getCurrentTileZ() {return currentTileZ; }
    public CacheStorage getCacheStorage() { return mainActivity.getCacheStorage(); }
    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }
    public MapStyle getMapStyle() {
        return mapStyle;
    }
    public MapTilesShower getMapTilesShower() {return mapTilesShower;}

    public MapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainActivity = (MainActivity) context;
        distanceToTileZ = new DistanceToTileZ(maxTileZoom);

        float initScaleFactor = distanceToTileZ.calcDistanceForZ(1);
        mapScaleListener = new MapScaleListener(initScaleFactor);
        currentTileZ = distanceToTileZ.calcCurrentTileZ(initScaleFactor);

        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                return true;
            }
        });
        mapTilesShower = new MapTilesShower(
                this
        );

        setEGLContextClientVersion(2);
        mapRenderer = new MapRenderer(
                mainActivity,
                initScaleFactor,
                new GlSurfaceCreatedHandler(this),
                new GlSurfaceChangedHandler(this)
        );
        setRenderer(mapRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        float screenX = event.getX();
        float screenY = event.getY();
        int action = event.getAction();
        MapWorldCamera mapWorldCamera = mapRenderer.getMapWorldCamera();

        if(event.getPointerCount() == 2) {
            float scaleFactorNormalized = mapScaleListener.getNormalizedScaleFactor();
            Log.d("GL_ARTEM", "Normalized scale factor = " + scaleFactorNormalized);
            if(action == MotionEvent.ACTION_MOVE) {
                // Маштабирование карты
                mapWorldCamera.moveZ(scaleFactorNormalized);
            } else if(action == 6 || action == 262) {
                int newTileZ = distanceToTileZ.calcCurrentTileZ(scaleFactorNormalized);
                if(newTileZ != currentTileZ) {
                    currentTileZ = newTileZ;
                    sendCurrentMapStateToShower();
                    Log.d("GL_ARTEM", "next tile z = " + currentTileZ);
                }
            }
            //Log.d("GL_ARTEM", "event = " + event.getAction());
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            float dx = previousScreenX - screenX;
            float dy = previousScreenY - screenY;
            if(dx < 60 && dy < 60) {
                float glWorldWidth = mapWorldCamera.getGlWorldWidth();
                currentMapX += dx * glWorldWidth * moveSpeed;
                currentMapY -= dy * glWorldWidth * moveSpeed;
                mapWorldCamera.moveXY(currentMapX, currentMapY);
                float[] coordinates = mapWorldCamera.getBottomRightWorldViewCoordinates();
                //Log.d("GL_ARTEM", "0, 0 screen world loc: " + Arrays.toString(coordinates));
            }
        }

        previousScreenX = screenX;
        previousScreenY = screenY;
        requestRender();
        return true;
    }

    public void sendCurrentMapStateToShower() {
        mapTilesShower.nextMapState(new MapTilesShowerMapState(
                currentTileZ, getMapRenderer().getMapWorldCamera()
        ));
    }
}
