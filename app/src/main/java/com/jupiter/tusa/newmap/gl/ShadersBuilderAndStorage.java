package com.jupiter.tusa.newmap.gl;

import android.opengl.GLES20;

import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.newmap.draw.LoadShader;
import com.jupiter.tusa.utils.ReadTextFromResource;

import java.util.HashMap;
import java.util.Map;

public class ShadersBuilderAndStorage {
    private final Map<String, ShaderPointers> shaders = new HashMap<String, ShaderPointers>();
    private final MainActivity mainActivity;
    public ShadersBuilderAndStorage(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void saveAndBuild(String key, String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = LoadShader.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = LoadShader.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        shaders.put(key, new ShaderPointers(vertexShader, fragmentShader));
    }

    public void saveAndBuild(String key, int vertexRes, int fragmentRes) {
        String fragmentCode = ReadTextFromResource.readText(mainActivity, fragmentRes);
        String vertexCode = ReadTextFromResource.readText(mainActivity, vertexRes);
        saveAndBuild(key, vertexCode, fragmentCode);
    }

    public ShaderPointers get(String key) {
        return shaders.get(key);
    }
}
