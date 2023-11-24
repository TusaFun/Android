package com.jupiter.tusa.newmap.mvt;

public class MvtObjectStyled {
    private MvtObject mvtObject;
    private float[] color;

    public MvtObject getMvtObject() {return mvtObject;}
    public float[] getColor() {return color;}

    public MvtObjectStyled(MvtObject mvtObject, float[] color) {
        this.mvtObject = mvtObject;
        this.color = color;
    }
}
