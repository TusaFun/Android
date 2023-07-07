#include <jni.h>
#include <string>
#include <iostream>
#include <cstdio>
#include <csetjmp>
#include "tusa-jpeg.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_jupiter_tusa_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jint JNICALL
Java_com_jupiter_tusa_MainActivity_processImage(
        JNIEnv* env, jobject thisz, jstring imagePath, jstring imagePathOut
) {
    const char* image_path_ptr = env->GetStringUTFChars(imagePath, nullptr);
    const char* image_path_out_ptr = env->GetStringUTFChars(imagePathOut, nullptr);

    char** argv = new char*[6];
    argv[0] = const_cast<char*>("program");
    argv[1] = const_cast<char*>(image_path_ptr);
    argv[2] = const_cast<char*>(image_path_out_ptr);
    argv[3] = const_cast<char*>("-q");
    argv[4] = const_cast<char*>("50");
    argv[5] = nullptr;
    //int result = compressImage(5, argv);
    int result = simpleCompress(image_path_ptr, image_path_out_ptr);

    //delete[] argv;
    return result;
}

