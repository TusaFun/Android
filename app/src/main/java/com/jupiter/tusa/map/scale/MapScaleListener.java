package com.jupiter.tusa.map.scale;

import android.view.ScaleGestureDetector;

public class MapScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float mScaleFactor = 1f;

    private float maxScale = 5.0f;
    private float minScale = 0.1f;

    public void setScaleFactor(float scaleFactor) {
        this.mScaleFactor = scaleFactor;
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public MapScaleListener(float startScaleFactor, float minScale, float maxScale) {
        this.mScaleFactor = startScaleFactor;
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= 1 / detector.getScaleFactor();

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(minScale, Math.min(mScaleFactor, maxScale));

        return true;
    }
}
