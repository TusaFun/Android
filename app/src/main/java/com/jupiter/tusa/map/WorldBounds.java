package com.jupiter.tusa.map;

public class WorldBounds {
    public WorldBounds(float topLeftXWorld, float topLeftYWorld, float bottomRightXWorld, float bottomRightYWorld) {
        this.topLeftXWorld = topLeftXWorld;
        this.topLeftYWorld = topLeftYWorld;
        this.bottomRightXWorld = bottomRightXWorld;
        this.bottomRightYWorld = bottomRightYWorld;
    }

    float topLeftXWorld;
    float topLeftYWorld;
    float bottomRightXWorld;
    float bottomRightYWorld;
}
