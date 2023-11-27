package com.jupiter.tusa.newmap.mvt;

import android.util.Log;

import com.jupiter.tusa.newmap.draw.MapStyleParameters;

public class MvtObjectStyled extends MvtObject{
    private final MapStyleParameters parameters;

    public MapStyleParameters getParameters() {return parameters;}

    public MvtObjectStyled(MvtObject from, MapStyleParameters mapStyleParameters) {
        super(from.vertices, from.drawOrder, from.layerName, from.tags, from.shape);
        this.parameters = mapStyleParameters;
        insertZ(mapStyleParameters.getZ());
    }

    private void insertZ(float z) {
        if(coordinatesPerVertex == 3) {
            Log.e("GL_ARTEM", "Already z inserted.");
            return;
        }
        vertices = MvtUtils.insertZOrderTo2D(vertices, z);
        coordinatesPerVertex = 3;
    }
}
