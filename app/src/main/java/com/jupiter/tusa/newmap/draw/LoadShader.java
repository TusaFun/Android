package com.jupiter.tusa.newmap.draw;

import android.opengl.GLES20;

public class LoadShader {
    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
