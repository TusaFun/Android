package com.jupiter.tusa.map.scale;

import android.view.ScaleGestureDetector;

public class MapScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float mScaleFactor = 1.f;

    public float getScaleFactor() {
        return mScaleFactor;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

        return true;
    }
}
