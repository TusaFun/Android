//
// Created by Artem on 20.12.2023.
//

#ifndef TUSA_SHADERS_BUCKET_H
#define TUSA_SHADERS_BUCKET_H

#include <map>
#include <android/asset_manager.h>
#include "plain_shader.h"
#include "shaders_bucket.h"

class ShadersBucket {
public:
    ShadersBucket();
    void loadShaders(AAssetManager* assetManager);
    const char* getShaderCode(const char* shader);
    PlainShader* createPlainShader();
    void compileAllShaders();
private:
    std::map<const char*, const char*> shadersCode = {};
    const char* loadShader(const char* filename, AAssetManager* assetManager);
    PlainShader* plainShader;
};


#endif //TUSA_SHADERS_BUCKET_H
