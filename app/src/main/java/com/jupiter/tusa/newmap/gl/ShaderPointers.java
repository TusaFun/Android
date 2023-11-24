package com.jupiter.tusa.newmap.gl;

public class ShaderPointers {
    private int vertexShader;
    private int fragmentShader;
    public ShaderPointers(int vertexShader, int fragmentShader) {
        this.fragmentShader = fragmentShader;
        this.vertexShader = vertexShader;
    }

    public int getVertexShader() {
        return vertexShader;
    }

    public int getFragmentShader() {
        return fragmentShader;
    }
}
