package com.jupiter.tusa.newmap.gl;

import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicInput;
import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicProgram;

import java.util.ArrayList;
import java.util.List;

public class DrawFrame {
    private final List<DrawOpenGlProgram> drawMePleases = new ArrayList<>();

    private final List<FDOFloatBasicInput> fdoFloatBasicInputs = new ArrayList<>();
    private final List<FDOFloatBasicInput> newFdoFloatBasicInputs = new ArrayList<>();
    private final ShadersBuilderAndStorage shadersBuilderAndStorage;

    public synchronized void addRenderProgramsInput(List<FDOFloatBasicInput> fdoFloatBasicInput) {
        newFdoFloatBasicInputs.addAll(fdoFloatBasicInput);
    }

    public List<DrawOpenGlProgram> getDrawMePlease() {return drawMePleases;}

    public DrawFrame(ShadersBuilderAndStorage shadersBuilderAndStorage) {
        this.shadersBuilderAndStorage = shadersBuilderAndStorage;
    }

    public void createOpenGlDrawPrograms() {
        fdoFloatBasicInputs.clear();
        fdoFloatBasicInputs.addAll(newFdoFloatBasicInputs);
        newFdoFloatBasicInputs.clear();

        ShaderPointers basicShader = shadersBuilderAndStorage.get("basic");
        FDOFloatBasicProgram fdoFloatBasicProgram = new FDOFloatBasicProgram(
                fdoFloatBasicInputs,
                basicShader.getFragmentShader(),
                basicShader.getVertexShader()
        );
        drawMePleases.clear();
        drawMePleases.add(fdoFloatBasicProgram);
    }
}
