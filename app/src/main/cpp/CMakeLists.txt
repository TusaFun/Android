# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html


cmake_minimum_required(VERSION 3.18.1)

project("tusa")

add_library(turbojpeg STATIC IMPORTED )
set_target_properties(turbojpeg
        PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/turbojpeg/${ANDROID_ABI}/libturbojpeg.a
#        INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_SOURCE_DIR}/turbojpeg/include
)

add_library(exif STATIC IMPORTED )
set_target_properties(exif
        PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/exif/${ANDROID_ABI}/libexif.a
#        INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_SOURCE_DIR}/exif/include
)

#add_library(triangle OBJECT ${CMAKE_SOURCE_DIR}/exif/${ANDROID_ABI}/triangle.o)

add_library(tusa SHARED
        native-lib.cpp
        tusa-jpeg.cpp
)

#add_custom_target(triangle ALL DEPENDS ${CMAKE_SOURCE_DIR}/triangle/${ANDROID_ABI}/triangle.o)
#add_dependencies(tusa triangle)
#add_library(triangle STATIC ${CMAKE_SOURCE_DIR}/triangle/${ANDROID_ABI}/triangle.o)
#set_target_properties(triangle PROPERTIES LINKER_LANGUAGE C)

target_include_directories(tusa PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

set(TRIANGLE_PATH ${CMAKE_SOURCE_DIR}/triangle/${ANDROID_ABI}/triangle.o)

find_library(log-lib  log)
target_link_libraries(tusa PUBLIC ${log-lib} exif turbojpeg  ${TRIANGLE_PATH})








