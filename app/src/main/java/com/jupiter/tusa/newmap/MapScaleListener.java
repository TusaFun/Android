package com.jupiter.tusa.newmap;

import android.view.ScaleGestureDetector;

public class MapScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private final float maxScale = 1100f;
    private final float minScale = 100f;
    private float mScaleFactor = 1f;
    private float scaleSpeed = 0.25f;

    public float getNormalizedScaleFactor() {return (mScaleFactor - minScale) / (maxScale - minScale); }
    public void setScaleFactor(float normalized) { mScaleFactor = minScale + normalized * (maxScale - minScale); }
    public void setScaleSpeed(float speed) {scaleSpeed = speed; }

    public MapScaleListener(float scaleNormalized) {
        setScaleFactor(scaleNormalized);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= 1 / ((detector.getScaleFactor() - 1) * scaleSpeed + 1);

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(minScale, Math.min(mScaleFactor, maxScale));

        return true;
    }
}
