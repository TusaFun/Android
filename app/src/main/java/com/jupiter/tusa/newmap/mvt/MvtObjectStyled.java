package com.jupiter.tusa.newmap.mvt;

import com.jupiter.tusa.newmap.draw.MapStyleParameters;

public class MvtObjectStyled {
    private MvtObject mvtObject;
    private MapStyleParameters parameters;

    public MvtObject getMvtObject() {return mvtObject;}
    public MapStyleParameters getParameters() {return parameters;}

    public MvtObjectStyled(MvtObject mvtObject, MapStyleParameters mapStyleParameters) {
        this.mvtObject = mvtObject;
        this.parameters = mapStyleParameters;
    }
}
