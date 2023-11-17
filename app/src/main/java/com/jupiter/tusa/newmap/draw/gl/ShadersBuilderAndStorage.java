package com.jupiter.tusa.newmap.draw.gl;

import android.graphics.Shader;
import android.opengl.GLES20;

import com.jupiter.tusa.newmap.draw.LoadShader;
import java.util.HashMap;
import java.util.Map;

public class ShadersStorage {
    private Map<String, ShaderPointers> shaders = new HashMap<String, ShaderPointers>();
    public void addShader(String key, String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = LoadShader.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = LoadShader.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        shaders.put(key, new ShaderPointers(vertexShader, fragmentShader));
    }

    public ShaderPointers get(String key) {
        return shaders.get(key);
    }
}
