package com.jupiter.tusa.newmap;

import java.util.Locale;

public class MvtApiResource {
    private String url = "https://api.mapbox.com/v4/%s/%d/%d/%d.mvt?access_token=%s";
    public String style = "mapbox.mapbox-streets-v8";
    public int zoom;
    public int x;
    public int y;
    public String accessToken = "pk.eyJ1IjoiaW52ZWN0eXMiLCJhIjoiY2w0emRzYWx5MG1iMzNlbW91eWRwZzdldCJ9.EAByLTrB_zc7-ytI6GDGBw";

    public MvtApiResource(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    public String makeUrl() {
        return String.format(Locale.ENGLISH, url, style, zoom, x, y, accessToken);
    }

    public String tileKey() {
        return style + zoom + x + y;
    }
}
