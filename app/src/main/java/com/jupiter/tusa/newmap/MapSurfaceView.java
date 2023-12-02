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
import com.jupiter.tusa.newmap.camera.MapWorldCamera;
import com.jupiter.tusa.newmap.chunk.ChunksWindow;
import com.jupiter.tusa.newmap.draw.MapStyle;
import com.jupiter.tusa.newmap.event.GlSurfaceChangedEvent;
import com.jupiter.tusa.newmap.event.GlSurfaceCreatedEvent;

import org.checkerframework.checker.units.qual.C;

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
    private TileWorldCoordinates tileWorldCoordinates;

    private int maxTileZoom = 19;

    public TileWorldCoordinates getTileWorldCoordinates() {return tileWorldCoordinates; }
    public MainActivity getMainActivity() {return mainActivity;}
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
        tileWorldCoordinates = new TileWorldCoordinates();

        ChunksWindow chunksWindow = new ChunksWindow(3,3);
        chunksWindow.shiftWindow(0, 0, 0);

        float initScaleFactor = distanceToTileZ.calcDistanceForZ(1);
        mapScaleListener = new MapScaleListener(initScaleFactor);
        int mapZ = distanceToTileZ.calcCurrentTileZ(initScaleFactor);
        tileWorldCoordinates.updateMapZ(mapZ);

        mScaleDetector = new ScaleGestureDetector(context, mapScaleListener);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                return true;
            }
        });
        mapTilesShower = new MapTilesShower(this);

        setEGLContextClientVersion(2);
        mapRenderer = new MapRenderer(
                this,
                initScaleFactor,
                new GlSurfaceCreatedEvent(this),
                new GlSurfaceChangedEvent(this)
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
            //Log.d("GL_ARTEM", "Normalized scale factor = " + scaleFactorNormalized);
            if(action == MotionEvent.ACTION_MOVE) {
                // Маштабирование карты
                mapWorldCamera.moveZ(scaleFactorNormalized);
                int newTileZ = distanceToTileZ.calcCurrentTileZ(scaleFactorNormalized);
                if(newTileZ != tileWorldCoordinates.getCurrentZ()) {
                    tileWorldCoordinates.updateMapZ(newTileZ);
                    mapTilesShower.nextMapState();
                    Log.d("GL_ARTEM", "next tile z = " + tileWorldCoordinates.getCurrentZ());
                }
            } else if(action == 6 || action == 262) {

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
                boolean changed = tileWorldCoordinates.updateCurrentTileXY(currentMapX, currentMapY);
                if(changed) {
                    //mapTilesShower.nextMapState();
                }

                float[] coordinates = mapWorldCamera.getBottomRightWorldViewCoordinates();

                //Log.d("GL_ARTEM", "0, 0 screen world loc: " + Arrays.toString(coordinates));
            }
        }

        previousScreenX = screenX;
        previousScreenY = screenY;
        requestRender();
        return true;
    }
}
