package com.jupiter.tusa.newmap.load.tiles;

import java.util.Locale;

public class MvtApiResource {
    public String style = "mapbox.mapbox-streets-v8,mapbox.mapbox-terrain-v2";
    public int z;
    public int x;
    public int y;
    public String accessToken = "pk.eyJ1IjoiaW52ZWN0eXMiLCJhIjoiY2w0emRzYWx5MG1iMzNlbW91eWRwZzdldCJ9.EAByLTrB_zc7-ytI6GDGBw";

    public MvtApiResource(int zoom, int x, int y) {
        this.z = zoom;
        this.x = x;
        this.y = y;
    }

    public String makeUrl() {
        String url = "https://api.mapbox.com/v4/%s/%d/%d/%d.mvt?access_token=%s";
        return String.format(Locale.ENGLISH, url, style, z, x, y, accessToken);
    }

    public String tileKey() {
        return getStyleKey() + z + x + y;
    }

    private String getStyleKey() {
        if ("mapbox.mapbox-streets-v8".equals(style)) {
            return "1";
        }
        return "0";
    }
}
