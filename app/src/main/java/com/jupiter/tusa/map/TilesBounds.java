package com.jupiter.tusa.map;

public class TilesBounds {
    final int topLeftX;
    final int topLeftY;
    final int bottomRightX;
    final int bottomRightY;

    public TilesBounds(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
    }
}
