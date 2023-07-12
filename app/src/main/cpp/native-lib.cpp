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

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_jupiter_tusa_MainActivity_compressJpegImage(
        JNIEnv* env, jobject thisz, jbyteArray byteArray, jint quality
) {
    unsigned char *inputImageCharBuffer, *outputImageBuffer;
    jsize inputImageSize;
    jbyte *inputImageJByte;
    int outputImageBufferSize;
    jbyteArray outputByteArrayImage;

    inputImageSize = env->GetArrayLength(byteArray);
    inputImageJByte = env->GetByteArrayElements(byteArray, JNI_FALSE);
    inputImageCharBuffer = (unsigned  char*) malloc(inputImageSize + 1);
    memcpy(inputImageCharBuffer, inputImageJByte, inputImageSize);
    inputImageCharBuffer[inputImageSize] = '\0';

    outputImageBuffer = simpleCompress(inputImageCharBuffer, inputImageSize, &outputImageBufferSize, quality);

    outputByteArrayImage = env->NewByteArray(outputImageBufferSize);
    env->SetByteArrayRegion(outputByteArrayImage, 0, outputImageBufferSize, (jbyte*) outputImageBuffer);

    free(inputImageCharBuffer);
    free(outputImageBuffer);
    env->ReleaseByteArrayElements(byteArray, inputImageJByte, JNI_ABORT);

    return outputByteArrayImage;
}

