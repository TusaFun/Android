//
// Created by Artem on 01.01.2024.
//

#ifndef TUSA_MAP_H
#define TUSA_MAP_H

#include "../shader/shaders_bucket.h"
#include "renderer/renderer.h"

class Map {
public:
    Map() {}

    // load shaders
    void init(AAssetManager* assetManager);
private:
    ShadersBucket shadersBucket = ShadersBucket();
    Renderer* renderer;
};


#endif //TUSA_MAP_H
