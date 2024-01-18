//
// Created by Artem on 20.12.2023.
//

#include "shader/shaders_bucket.h"
#include "shader/shaders_bucket.h"
#include <iostream>
#include <fstream>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

void ShadersBucket::loadShaders(AAssetManager* assetManager) {
    shadersCode["plain.vert"] = loadShader("shaders/plain.vert", assetManager);
    shadersCode["plain.frag"] = loadShader("shaders/plain.frag", assetManager);
}

const char *ShadersBucket::loadShader(const char *filename, AAssetManager* assetManager) {
    AAsset* asset = AAssetManager_open(assetManager, filename, AASSET_MODE_BUFFER);
    const char* shaderData = static_cast<const char *>(AAsset_getBuffer(asset));
    AAsset_close(asset);
    return shaderData;
}

const char *ShadersBucket::getShaderCode(const char *shader) {
    return shadersCode[shader];
}

ShadersBucket::ShadersBucket() {}

PlainShader* ShadersBucket::createPlainShader() {
    return new PlainShader(shadersCode["plain.vert"], shadersCode["plain.frag"]);
}

void ShadersBucket::compileAllShaders() {
    plainShader = createPlainShader();
}


