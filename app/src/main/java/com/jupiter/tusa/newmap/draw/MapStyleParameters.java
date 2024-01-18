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

    public void setZ(float z) {this.z = z;}

    public void setClassValue(String classValue) {
        this.classValue = classValue;
    }

    public String getClassValue() {
        return classValue;
    }

    public float getZ() {return z;}

    private float[] color;
    private float lineWidth;
    private float z;
    private String classValue;
}
