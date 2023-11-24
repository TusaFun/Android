package com.jupiter.tusa.newmap.mvt;

import java.util.List;

public class Vertices {
    public List<Float> vertices;
    private Boolean clockwise = null;
    public Boolean getClockwise() {
        if(clockwise != null) {
            return clockwise;
        }
        clockwise = MvtUtils.isClockwise(vertices);
        return clockwise;
    }
    public Vertices(List<Float> vertices) {
        this.vertices = vertices;
    }
}
