package com.jupiter.tusa.newmap.draw;

import java.util.ArrayList;
import java.util.List;

public class MapStyle {
    private int forTileZ;
    public int getForTileZ(){return forTileZ;}
    public MapStyle(int forTileZ, List<String> showLayers) {
        this.forTileZ = forTileZ;
        this.showLayers = showLayers;
    }

    public float[] getColor(String name) {
        switch (name) {
            case "water": return new float[]{ 0f, 0.76953125f, 1f, 1.0f };
            case "waterway": return new float[]{ 0f, 0.71953125f, 1f, 1.0f };
            case "admin": return new float[] { 0f, 0f, 0f, 1f };
            case "landuse": return new float[] {0.729f, 0.62f, 0, 1f};
            case "landuse_overlay": return new float[] {1f, 0f, 0, 1f};
            case "road": return new float[] {0.067f, 0.102f, 0, 0.361f};
            case "aeroway": return new float[] {1f, 0.102f, 0, 1f};
            case "building": return new float[] {0f, 1f, 0.169f, 0.639f, 1f};
            default: return new float[] {1f, 0, 0, 1f};
        }
    }

    public List<String> showLayers = new ArrayList<String>() {
        {
            add("water");
            add("admin");
            add("landuse");
            add("road");
            add("landuse_overlay");
            add("waterway");
            add("aeroway");
            add("transit_stop_label");
            add("poi_label");
            add("place_label");
            add("motorway_junction");
            add("building");
            add("building");
            add("housenum_label");
            add("structure");
        }
    };
}
