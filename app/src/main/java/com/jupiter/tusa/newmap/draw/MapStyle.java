package com.jupiter.tusa.newmap.draw;

import android.util.Log;

import com.google.android.gms.common.util.MapUtils;
import com.jupiter.tusa.newmap.ColorUtils;
import com.jupiter.tusa.newmap.mvt.MvtUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import vector_tile.VectorTile;

public class MapStyle {
    public List<String> showLayers = new ArrayList<String>()
    {{
        add("natural_label");
        add("landcover");
        add("hillshade");
        add("water");
        //add("landuse");
        add("road");
        //add("landuse_overlay");
        add("waterway");
        add("aeroway");
        add("transit_stop_label");
        add("poi_label");
        add("place_label");
        add("motorway_junction");
        add("building");
        add("housenum_label");
        add("structure");
        add("admin");
    }};

    public MapStyleParameters getStyleParametersForFeature(String name, Map<String, VectorTile.Tile.Value> tags, int zoom) {
        MapStyleParameters mapStyleParameters = new MapStyleParameters();
        if(tags.containsKey("class")) {
            String classValue = Objects.requireNonNull(tags.get("class")).getStringValue();
            mapStyleParameters.setClassValue(classValue);
        }
        if(Objects.equals(name, "landcover")) {
            mapStyleParameters.setZ(0f);
            if(tags.containsKey("class")) {
                String classValue = Objects.requireNonNull(tags.get("class")).getStringValue();
                if(classValue.equals("wood")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(105, 56, 72, 0.8f));
                    mapStyleParameters.setZ(0.1f);
                    return mapStyleParameters;
                }
                if(classValue.equals("snow")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(197, 68, 88, 1f));
                    return mapStyleParameters;
                }
            }
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 57, 89, 1));
            return mapStyleParameters;
        }



        if(name.equals("hillshade")) {
            mapStyleParameters.setZ(0.02f);
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(86, 25, 57, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "land")) {
            mapStyleParameters.setZ(0.03f);
            if(zoom == 9) {
                mapStyleParameters.setColor(ColorUtils.hslaToRgba(60, 0, 100, 1));
                return mapStyleParameters;
            }
            if(zoom == 11) {
                mapStyleParameters.setColor(ColorUtils.hslaToRgba(60, 0, 99, 1));
                return mapStyleParameters;
            }
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(60, 0, 99, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "waterway")) {
            mapStyleParameters.setZ(0.95f);
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(197, 98, 78, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "landuse") && zoom == 15 || zoom == 16) {
            mapStyleParameters.setZ(0.04f);
            if(tags.containsKey("class")) {
                String classValue = Objects.requireNonNull(tags.get("class")).getStringValue();
                if(classValue.equals("wood")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(105, 66, 82, 0.8f));
                    return mapStyleParameters;
                }
                if(classValue.equals("scrub")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 63, 90, 0.6f));
                    return mapStyleParameters;
                }
                if(classValue.equals("agriculture")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 66, 96, 0.6f));
                    return mapStyleParameters;
                }
                if(classValue.equals("park")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 71, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("grass")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 66, 96, 0.6f));
                    return mapStyleParameters;
                }
                if(classValue.equals("airport")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(244, 54, 97, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("cemetery")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 50, 85, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("glacier")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(197, 68, 88, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("hospital")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(320, 30, 95, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("pitch")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(90, 81, 85, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("sand")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 76, 94, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("school")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(35, 30, 92, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("commercial_area")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(35, 80, 95, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("parking")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(0, 0, 96, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("residential")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(60, 0, 100, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("facility") || classValue.equals("industrial")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(240, 30, 97, 1f));
                    return mapStyleParameters;
                }
            }
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(60, 2, 94, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "pitch-outline")) {
            mapStyleParameters.setZ(0.05f);
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(90, 76, 75, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "national-park")) {
            mapStyleParameters.setZ(0.06f);
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(100, 37, 78, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "road")) {
            mapStyleParameters.setZ(0.9f);
            if(tags.containsKey("class")) {
                String classValue = Objects.requireNonNull(tags.get("class")).getStringValue();
                if(classValue.equals("motorway")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("motorway_link")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("trunk_link")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("track")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("primary")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(230, 10, 92, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("primary_link")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(230, 10, 92, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("service")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("trunk")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("secondary")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(230, 10, 92, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("tertiary")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("street")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("street_limited")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("pedestrian")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("ferry")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                if(classValue.equals("major_rail")) {
                    mapStyleParameters.setColor(ColorUtils.hslaToRgba(45, 98, 80, 1f));
                    return mapStyleParameters;
                }
                Log.w("GL_ARTEM", "road skipped class " + classValue);
            }
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(230, 10, 92, 1f));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "water")) {
            mapStyleParameters.setZ(0.91f);
            mapStyleParameters.setColor(ColorUtils.hslaToRgba(197, 98, 78, 1));
            return mapStyleParameters;
        }

        if(Objects.equals(name, "admin")) {
            mapStyleParameters.setZ(0.99f);
            if(zoom > 12) {
                mapStyleParameters.setColor(new float[] {0f,0f,0f,0f});
                return mapStyleParameters;
            }
            boolean disputed = Objects.requireNonNull(tags.get("disputed")).getBoolValue();
            String iso = Objects.requireNonNull(tags.get("iso_3166_1")).getStringValue();
            boolean insideCountry = !iso.contains("-");
            float adminWidth = 4.5f;
            if(insideCountry) {
                mapStyleParameters.setLineWidth(adminWidth / 2);
                mapStyleParameters.setColor(ColorUtils.hslaToRgba(250, 90, 85, 1f));
            } else {
                mapStyleParameters.setColor(ColorUtils.hslaToRgba(250, 90, 80, 1f));
                mapStyleParameters.setLineWidth(adminWidth);
            }

            if(disputed) {
                mapStyleParameters.setColor(ColorUtils.hslaToRgba(250, 90, 80, 1f));
                mapStyleParameters.setLineWidth(adminWidth / 2);
            }
            return mapStyleParameters;
        }

        //Log.d("GL_ARTEM", "No style for " + name);
        mapStyleParameters.setColor(new float[] {1f, 0, 0, 0f});
        return mapStyleParameters;
    }
}
