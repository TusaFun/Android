package com.jupiter.tusa.newmap.gl;

import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicInput;

import java.util.List;

public class TileDataForPrograms {
    private final List<FDOFloatBasicInput> fdoFloatBasicInputs;
    public List<FDOFloatBasicInput> getFdoFloatBasicInputs() {return fdoFloatBasicInputs;}
    public TileDataForPrograms(List<FDOFloatBasicInput> fdoFloatBasicInputs) {
        this.fdoFloatBasicInputs = fdoFloatBasicInputs;
    }
}
