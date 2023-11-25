package com.jupiter.tusa.newmap.draw;

public class MapStyleParameters {
    public float[] getColor() {return color;}

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    private float[] color;
    private float lineWidth;
}
