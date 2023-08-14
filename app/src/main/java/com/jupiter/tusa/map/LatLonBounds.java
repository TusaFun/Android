package com.jupiter.tusa.map;

public class LatLonBounds {

    private final float topLeftLatitude;
    private final float topLeftLongitude;

    private final float bottomRightLatitude;
    private final float bottomRightLongitude;

    public float getTopLeftLatitude() {
        return topLeftLatitude;
    }

    public float getTopLeftLongitude() {
        return topLeftLongitude;
    }

    public float getBottomRightLatitude() {
        return  bottomRightLatitude;
    }

    public float getBottomRightLongitude() {
        return  bottomRightLongitude;
    }

    public LatLonBounds(float topLeftLatitude, float topLeftLongitude, float bottomRightLatitude, float bottomRightLongitude) {
        this.topLeftLatitude = topLeftLatitude;
        this.topLeftLongitude = topLeftLongitude;
        this.bottomRightLatitude = bottomRightLatitude;
        this.bottomRightLongitude = bottomRightLongitude;
    }
}
