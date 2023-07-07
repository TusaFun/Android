# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html


cmake_minimum_required(VERSION 3.18.1)

project("tusa")

add_library(turbojpeg STATIC IMPORTED )
set_target_properties(turbojpeg
        PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/turbojpeg/${ANDROID_ABI}/libturbojpeg.a
        INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_SOURCE_DIR}/turbojpeg/include
)

add_library(tusa SHARED
        native-lib.cpp
        tusa-jpeg.cpp
)
target_include_directories(tusa PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

find_library(log-lib  log)
target_link_libraries(tusa PUBLIC ${log-lib} turbojpeg)






