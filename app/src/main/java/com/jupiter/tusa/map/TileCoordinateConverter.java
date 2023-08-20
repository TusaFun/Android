package com.jupiter.tusa.map;

public class TileCoordinateConverter {
    public static int[] convertToTileCoordinates(double latitude, double longitude, int zoom) {
        int tileCount = (int) Math.pow(2, zoom);
        double x = (longitude + 180.0) / 360.0 * tileCount;
        double latRad = Math.toRadians(latitude);
        double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * tileCount;

        int tileX = (int) Math.floor(x);
        int tileY = (int) Math.floor(y);

        return new int[] { tileX, tileY };
    }

    public static float worldXToLongitude(float worldX) {
        float longitudeRange = 180.0f;
        return worldX * longitudeRange;
    }

    public static float worldYToLatitude(float worldY) {
        float longitudeRange = 90.0f;
        return worldY * longitudeRange;
    }
}
