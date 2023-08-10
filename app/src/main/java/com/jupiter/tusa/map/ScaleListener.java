package com.jupiter.tusa.map;

import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private float scaleFactor = 1f;
    private float previousScaleFactor = 1f;
    private float deltaScale = 0f;

    public float getScaleFactor() {
        return scaleFactor;
    }

    public float getDeltaScale() {
        return deltaScale;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        deltaScale = scaleFactor - previousScaleFactor;
        previousScaleFactor = scaleFactor;

        //Log.d("GL_ARTEM", "scale factor = " + scaleFactor + " deltaScale = " + deltaScale);

        return true;
    }

}
