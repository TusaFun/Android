package com.jupiter.tusa.map.scale;

import android.view.ScaleGestureDetector;

public class MapScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float mScaleFactor = 1f;

    final private float maxScale = 5.0f;
    final private float minScale = 0.1f;

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.mScaleFactor = scaleFactor;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= 1 / detector.getScaleFactor();

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(minScale, Math.min(mScaleFactor, maxScale));

        return true;
    }
}
