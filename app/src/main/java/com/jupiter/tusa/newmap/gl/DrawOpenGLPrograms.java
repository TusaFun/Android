package com.jupiter.tusa.newmap.gl;

import com.jupiter.tusa.newmap.draw.DrawOpenGlProgram;
import com.jupiter.tusa.newmap.gl.program.FDOFloatBasicProgram;
import java.util.ArrayList;

public class DrawOpenGLPrograms {
    private final DrawOpenGlProgram[] drawPrograms = new DrawOpenGlProgram[1];
    private final ShadersBuilderAndStorage shadersBuilderAndStorage;

    public FDOFloatBasicProgram getFdoFloatBasicProgram() {return (FDOFloatBasicProgram) drawPrograms[0]; }
    public DrawOpenGlProgram[] getDrawPrograms() { return drawPrograms; }

    public DrawOpenGLPrograms(ShadersBuilderAndStorage shadersBuilderAndStorage) {
        this.shadersBuilderAndStorage = shadersBuilderAndStorage;
    }

    public void createOpenGlDrawPrograms() {
        ShaderPointers basicShader = shadersBuilderAndStorage.get("basic");
        drawPrograms[0] = new FDOFloatBasicProgram(
                new ArrayList<>(),
                basicShader.getFragmentShader(),
                basicShader.getVertexShader()
        );
    }
}
