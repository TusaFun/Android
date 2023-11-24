
#define REAL float

#include <jni.h>
#include <string>
#include <iostream>
#include <cstdio>
#include <csetjmp>
#include "tusa-jpeg.h"
#include "triangle.h"
#include <vector>
#include <android/log.h>
#include <cstdlib>

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



jobject triangulate(JNIEnv *env, jobject thiz, jfloatArray vertices, jintArray segments) {
    struct triangulateio in, mid, out, vorout;

    jsize inputPointsSize = env->GetArrayLength(vertices);
    float* inputPointsElements = env->GetFloatArrayElements(vertices, nullptr);

    jsize inputSegmentsSize = env->GetArrayLength(segments);
    int* inputSegmentsElements = env->GetIntArrayElements(segments, nullptr);

    in.numberofpoints = inputPointsSize/2;
    in.numberofpointattributes = 0;
    in.pointlist = (REAL *) malloc(in.numberofpoints * 2 * sizeof(REAL));
    for(int i = 0; i < in.numberofpoints * 2; i++) {
        in.pointlist[i] = inputPointsElements[i];
    }

    in.numberofsegments = inputSegmentsSize/2;
    in.segmentlist = (int *) malloc(in.numberofsegments * 2 * sizeof(int));
    for(int i = 0; i < in.numberofsegments * 2; i++) {
        in.segmentlist[i] = inputSegmentsElements[i];
    }

    env->ReleaseIntArrayElements(segments, inputSegmentsElements, 0);
    env->ReleaseFloatArrayElements(vertices, inputPointsElements, 0);

//    in.pointlist[0] = 0.0;
//    in.pointlist[1] = 0.0;
//    in.pointlist[2] = 1.0;
//    in.pointlist[3] = 0.0;
//    in.pointlist[4] = 1.0;
//    in.pointlist[5] = 10.0;
//    in.pointlist[6] = 0.0;
//    in.pointlist[7] = 10.0;
    in.pointattributelist = (REAL *) malloc(in.numberofpoints *
                                            in.numberofpointattributes *
                                            sizeof(REAL));
//    in.pointattributelist[0] = 0.0;
//    in.pointattributelist[1] = 1.0;
//    in.pointattributelist[2] = 11.0;
//    in.pointattributelist[3] = 10.0;
    in.pointmarkerlist = (int *) malloc(in.numberofpoints * sizeof(int));
//    in.pointmarkerlist[0] = 0;
//    in.pointmarkerlist[1] = 2;
//    in.pointmarkerlist[2] = 0;
//    in.pointmarkerlist[3] = 0;

    in.numberofholes = 0;
    in.numberofregions = 0;
    in.regionlist = (REAL *) malloc(in.numberofregions * 4 * sizeof(REAL));
//    in.regionlist[0] = 0.5;
//    in.regionlist[1] = 5.0;
//    in.regionlist[2] = 7.0;            /* Regional attribute (for whole mesh). */
//    in.regionlist[3] = 0.1;          /* Area constraint that will not be used. */

    printf("Input point set:\n\n");

    /* Make necessary initializations so that Triangle can return a */
    /*   triangulation in `mid' and a voronoi diagram in `vorout'.  */

    mid.pointlist = (REAL *) NULL;            /* Not needed if -N switch used. */
    /* Not needed if -N switch used or number of point attributes is zero: */
    mid.pointattributelist = (REAL *) NULL;
    mid.pointmarkerlist = (int *) NULL; /* Not needed if -N or -B switch used. */
    mid.trianglelist = (int *) NULL;          /* Not needed if -E switch used. */
    /* Not needed if -E switch used or number of triangle attributes is zero: */
    mid.triangleattributelist = (REAL *) NULL;
    mid.neighborlist = (int *) NULL;         /* Needed only if -n switch used. */
    /* Needed only if segments are output (-p or -c) and -P not used: */
    mid.segmentlist = (int *) NULL;
    /* Needed only if segments are output (-p or -c) and -P and -B not used: */
    mid.segmentmarkerlist = (int *) NULL;
    mid.edgelist = (int *) NULL;             /* Needed only if -e switch used. */
    mid.edgemarkerlist = (int *) NULL;   /* Needed if -e used and -B not used. */

    vorout.pointlist = (REAL *) NULL;        /* Needed only if -v switch used. */
    /* Needed only if -v switch used and number of attributes is not zero: */
    vorout.pointattributelist = (REAL *) NULL;
    vorout.edgelist = (int *) NULL;          /* Needed only if -v switch used. */
    vorout.normlist = (REAL *) NULL;         /* Needed only if -v switch used. */

    /* Triangulate the points.  Switches are chosen to read and write a  */
    /*   PSLG (p), preserve the convex hull (c), number everything from  */
    /*   zero (z), assign a regional attribute to each element (A), and  */
    /*   produce an edge list (e), a Voronoi diagram (v), and a triangle */
    /*   neighbor list (n).                                              */

    triangulate("pz", &in, &mid, &vorout);

    printf("Initial triangulation:\n\n");
    printf("Initial Voronoi diagram:\n\n");

    /* Attach area constraints to the triangles in preparation for */
    /*   refining the triangulation.                               */

    /* Needed only if -r and -a switches used: */
    mid.trianglearealist = (REAL *) malloc(mid.numberoftriangles * sizeof(REAL));
    mid.trianglearealist[0] = 3.0;
    mid.trianglearealist[1] = 1.0;

    /* Make necessary initializations so that Triangle can return a */
    /*   triangulation in `out'.                                    */

    out.pointlist = (REAL *) NULL;            /* Not needed if -N switch used. */
    /* Not needed if -N switch used or number of attributes is zero: */
    out.pointattributelist = (REAL *) NULL;
    out.trianglelist = (int *) NULL;          /* Not needed if -E switch used. */
    /* Not needed if -E switch used or number of triangle attributes is zero: */
    out.triangleattributelist = (REAL *) NULL;

    /* Refine the triangulation according to the attached */
    /*   triangle area constraints.                       */

    //triangulate("prazBP", &mid, &out, (struct triangulateio *) NULL);

    jfloatArray jPointsArray = env->NewFloatArray(mid.numberofpoints  * 2);
    env->SetFloatArrayRegion(jPointsArray, 0, mid.numberofpoints * 2, mid.pointlist);

    jintArray jTrianglesArray = env->NewIntArray(mid.numberoftriangles * 3);
    env->SetIntArrayRegion(jTrianglesArray, 0, mid.numberoftriangles * 3, mid.trianglelist);

    /* Free all allocated arrays, including those allocated by Triangle. */
    free(in.pointlist);
    free(in.pointattributelist);
    free(in.pointmarkerlist);
    free(in.regionlist);
    free(mid.pointlist);
    free(mid.pointattributelist);
    free(mid.pointmarkerlist);
    free(mid.trianglelist);
    free(mid.triangleattributelist);
    free(mid.trianglearealist);
    free(mid.neighborlist);
    free(mid.segmentlist);
    free(mid.segmentmarkerlist);
    free(mid.edgelist);
    free(mid.edgemarkerlist);
    free(vorout.pointlist);
    free(vorout.pointattributelist);
    free(vorout.edgelist);
    free(vorout.normlist);
    free(out.pointlist);
    free(out.pointattributelist);
    free(out.trianglelist);
    free(out.triangleattributelist);

    jclass wrapperClass = env->FindClass("com/jupiter/tusa/jnioutput/TriangulateOutput");
    jmethodID  constructor = env->GetMethodID(wrapperClass, "<init>", "([F[I)V");
    jobject result = env->NewObject(wrapperClass, constructor, jPointsArray, jTrianglesArray);
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_jupiter_tusa_MainActivity_triangulate(JNIEnv *env, jobject thiz, jobject arrayList) {
    // Get the class and method IDs for ArrayList and the TriangleInput class.
    jclass arrayListClass = env->GetObjectClass(arrayList);
    jclass triangleInputClass = env->FindClass("com/jupiter/tusa/jnioutput/TriangleInput");
    jmethodID getMethod = env->GetMethodID(arrayListClass, "get", "(I)Ljava/lang/Object;");
    jmethodID sizeMethod = env->GetMethodID(arrayListClass, "size", "()I");
    jint listSize = env->CallIntMethod(arrayList, sizeMethod);

    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    jobject arrayListOutput = env->NewObject(arrayListClass, arrayListConstructor);

    for (int i = 0; i < listSize; i++) {
        jobject triangleObject = env->CallObjectMethod( arrayList, getMethod, i);
        jfieldID verticesField = env->GetFieldID(triangleInputClass, "vertices", "[F");
        jfieldID segmentsField = env->GetFieldID(triangleInputClass, "segments", "[I");

        auto verticesArray = (jfloatArray)env->GetObjectField(triangleObject, verticesField);
        auto segmentsArray = (jintArray)env->GetObjectField(triangleObject, segmentsField);

        jobject triangleOutput = triangulate(env, thiz, verticesArray, segmentsArray);
        env->CallBooleanMethod(arrayListOutput, addMethod, triangleOutput);
    }

    return arrayListOutput;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_jupiter_tusa_MainActivity_triangulateOne(JNIEnv *env, jobject thiz, jfloatArray vertices, jintArray segments) {
    return triangulate(env, thiz, vertices, segments);
}