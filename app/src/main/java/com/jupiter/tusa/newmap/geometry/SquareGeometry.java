package com.jupiter.tusa.newmap.geometry;

public class SquareGeometry{

    private static int drawOrder[] = {0, 1, 2, 0, 2, 3};

    public static Geometry create(float halfSide) {
        float[] vertexCoordinates = new float[] {
                -halfSide, -halfSide,
                halfSide, -halfSide,
                halfSide, halfSide,
                -halfSide, halfSide,
        };
        return new Geometry(vertexCoordinates, 2, drawOrder);
    }
}
