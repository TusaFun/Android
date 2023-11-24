package com.jupiter.tusa.newmap.draw;

import java.util.ArrayList;

public class MapStyleList {
    MapStyle[] mapStyles;

    public MapStyleList() {
        mapStyles = new MapStyle[21];
        putStyle(makeDefaultStyle(0));
        putStyle(makeDefaultStyle(1));
        putStyle(makeDefaultStyle(2));
        putStyle(makeDefaultStyle(3));
        putStyle(makeDefaultStyle(4));
        putStyle(makeDefaultStyle(5));
        putStyle(makeDefaultStyle(6));
        putStyle(makeDefaultStyle(7));
        putStyle(makeDefaultStyle(8));
        putStyle(makeDefaultStyle(9));
        putStyle(makeDefaultStyle(10));
        putStyle(makeDefaultStyle(11));
        putStyle(makeDefaultStyle(12));
        putStyle(makeDefaultStyle(13));
        putStyle(makeDefaultStyle(14));
        putStyle(makeDefaultStyle(15));
        putStyle(makeDefaultStyle(16));
        putStyle(makeDefaultStyle(17));
        putStyle(makeDefaultStyle(18));
        putStyle(makeDefaultStyle(19));
        putStyle(makeDefaultStyle(20));
    }

    public MapStyle getStyleFor(int tileZ) {
        return mapStyles[tileZ];
    }

    private void putStyle(MapStyle mapStyle) {
        mapStyles[mapStyle.getForTileZ()] = mapStyle;
    }

    private MapStyle makeDefaultStyle(int tileZ) {
        return new MapStyle(tileZ,
                new ArrayList<String>() {
                    {
                        add("water");
                        add("admin");
                        add("landuse");
                        add("road");
//                        add("landuse_overlay");
                        add("waterway");
//                        add("aeroway");
//                        add("transit_stop_label");
//                        add("poi_label");
//                        add("place_label");
                        add("motorway_junction");
                        add("building");
//                        add("housenum_label");
                        add("structure");
                    }
                }
        );
    }
}
