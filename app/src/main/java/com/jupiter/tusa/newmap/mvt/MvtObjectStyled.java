package com.jupiter.tusa.newmap.mvt;

import android.util.Log;

import com.jupiter.tusa.newmap.draw.MapStyleParameters;

import java.util.List;

public class MvtObjectStyled extends MvtObject{
    private int key;
    private final MapStyleParameters parameters;

    public int getVertexStride() {
        return coordinatesPerVertex * sizeOfOneCoordinate;
    }

    public MapStyleParameters getParameters() {return parameters;}

    public MvtObjectStyled(int key, MvtObject from, MapStyleParameters mapStyleParameters) {
        super(from.vertices, from.drawOrder, from.layerName, from.tags, from.shape);
        this.parameters = mapStyleParameters;
        this.key = key;
        insertZ(mapStyleParameters.getZ());
    }

    public int getKey() {
        return key;
    }

    public String getStyleKey() {
        String key = layerName;
        if(parameters.getClassValue() != null) {
            key += parameters.getClassValue();
        }
        return key;
    }

    public void addAdditional(List<Float> vertices, List<Integer> drawOrder) {
        int startFrom = this.vertices.size() / 3;
        this.vertices.addAll(vertices);
        for(Integer d : drawOrder) {
            this.drawOrder.add(startFrom + d);
        }
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
