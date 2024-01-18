//
// Created by Artem on 01.01.2024.
//

#include "map/map.h"
#include "renderer/renderer.h"

void Map::init(AAssetManager* assetManager) {
    shadersBucket.loadShaders(assetManager);
    shadersBucket.compileAllShaders();
}
