package com.jupiter.tusa.newmap.gl;

import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicInput;
import com.jupiter.tusa.newmap.mvt.MvtObject;
import com.jupiter.tusa.newmap.mvt.MvtObjectStyled;

import java.util.List;

public class TileDataForPrograms {
    private final List<MvtObjectStyled> fdoFloatBasicInputs;
    public List<MvtObjectStyled> getMvtObjectStyled() {return fdoFloatBasicInputs;}
    public TileDataForPrograms(List<MvtObjectStyled> fdoFloatBasicInputs) {
        this.fdoFloatBasicInputs = fdoFloatBasicInputs;
    }
}
