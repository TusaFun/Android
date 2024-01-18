# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html


cmake_minimum_required(VERSION 3.22.1)

project("tusa")

set(PROTO_FILE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/proto/vector_tile.proto)
set(PROTO_GENERATED_PATH ${CMAKE_CURRENT_BINARY_DIR}/generated)

add_library(tusa SHARED
        native-lib.cpp

        shader/plain_shader.cpp
        shader/shaders_bucket.cpp
        shader/shader.cpp

        renderer/renderer.cpp

        util/frustrums.cpp
        util/matrices.cpp
        util/timer.cpp

        map/map.cpp

        csscolorparser/csscolorparser.cpp
)

target_include_directories(tusa PRIVATE include)

include(${PROJECT_SOURCE_DIR}/vendor/protozero.cmake)
include(${PROJECT_SOURCE_DIR}/vendor/vector-tile.cmake)
include(${PROJECT_SOURCE_DIR}/vendor/earcut.hpp.cmake)
include(${PROJECT_SOURCE_DIR}/vendor/boost.cmake)


target_link_libraries(tusa
        mbgl-vendor-protozero
        mbgl-vendor-vector-tile
        mbgl-vendor-earcut.hpp
        mbgl-vendor-boost
)

target_link_libraries(tusa log)
target_link_libraries(tusa GLESv2)
target_link_libraries(tusa android)
