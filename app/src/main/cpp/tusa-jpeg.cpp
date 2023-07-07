
#include "tusa-jpeg.h"
#include <android/log.h>

#ifdef _MSC_VER
#define _CRT_SECURE_NO_DEPRECATE
#endif

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cerrno>
#include <turbojpeg.h>


#ifdef _WIN32
#define strcasecmp  stricmp
#define strncasecmp  strnicmp
#endif




#define THROW_UNIX(action)  THROW(action, strerror(errno))

#define DEFAULT_SUBSAMP  TJSAMP_444
#define DEFAULT_QUALITY  75

tjhandle tjInstance = nullptr;

void tusaError(const char* action, const char* message) {
    printf("ERROR in line %d while %s:\n%s\n", __LINE__, action, message); \
  __android_log_print(ANDROID_LOG_DEBUG, "tusa-jpeg", "%s %s", action, message); \
}

int tusaTJError(const char* action) {
    tusaError(action, tj3GetErrorStr(tjInstance));
    return -1;
}

int tusaUnixError(const char* action) {
    tusaError(action, strerror(errno));
    return -1;
}

const char *subsampName[TJ_NUMSAMP] = {
        "4:4:4", "4:2:2", "4:2:0", "Grayscale", "4:4:0", "4:1:1", "4:4:1"
};

const char *colorspaceName[TJ_NUMCS] = {
        "RGB", "YCbCr", "GRAY", "CMYK", "YCCK"
};

tjscalingfactor *scalingFactors = nullptr;
int numScalingFactors = 0;


/* DCT filter example.  This produces a negative of the image. */

static int customFilter(short *coeffs, tjregion arrayRegion,
                        tjregion planeRegion, int componentIndex,
                        int transformIndex, tjtransform *transform)
{
    int i;

    for (i = 0; i < arrayRegion.w * arrayRegion.h; i++)
        coeffs[i] = -coeffs[i];

    return 0;
}

int simpleCompress(const char* inputFilePath, const char* outputFilePath) {
    int retval = 0, funcResult = 0;
    int fastUpsample = 0, fastDCT = 0;
    int width, height, inSubsamp, inColorspace;
    FILE* inputCompressedJpegFile, *outputCompressedJpegFile;
    size_t inputCompressedJpegSize;
    unsigned char* inputCompressedJpegBuffer = nullptr, *inputDecompressedJpegBuffer = nullptr, *outputCompressedJpegBuffer = nullptr;
    size_t fReadResult, outputCompressedJpegSize;


    // open compressed image jpeg file
    inputCompressedJpegFile = fopen(inputFilePath, "rb");
    if(inputCompressedJpegFile == nullptr) {
        fclose(inputCompressedJpegFile);
        return tusaUnixError("opening input file");
    }

    // read compressed image jpeg file
    fseek(inputCompressedJpegFile, 0, SEEK_END);
    inputCompressedJpegSize = ftell(inputCompressedJpegFile);
    fseek(inputCompressedJpegFile, 0, SEEK_SET);

    inputCompressedJpegBuffer = static_cast<unsigned char *>(tj3Alloc(inputCompressedJpegSize));
    if(inputCompressedJpegBuffer == nullptr) {
        return tusaUnixError("allocating JPEG buffer");
    }

    fReadResult = fread(inputCompressedJpegBuffer, inputCompressedJpegSize, 1, inputCompressedJpegFile);
    if(fReadResult < 1) {
        tusaUnixError("reading input file");
    }
    fclose(inputCompressedJpegFile);

    tjInstance = tj3Init(TJINIT_DECOMPRESS);

    tj3Set(tjInstance, TJPARAM_FASTUPSAMPLE, fastUpsample);
    tj3Set(tjInstance, TJPARAM_FASTDCT, fastDCT);

    tj3DecompressHeader(tjInstance, inputCompressedJpegBuffer, inputCompressedJpegSize);
    width = tj3Get(tjInstance, TJPARAM_JPEGWIDTH);
    height = tj3Get(tjInstance, TJPARAM_JPEGHEIGHT);
    inSubsamp = tj3Get(tjInstance, TJPARAM_SUBSAMP);
    inColorspace = tj3Get(tjInstance, TJPARAM_COLORSPACE);

    inputDecompressedJpegBuffer = (unsigned char*) malloc(sizeof(unsigned char) * width * height * tjPixelSize[TJPF_RGB]);
    funcResult = tj3Decompress8(tjInstance, inputCompressedJpegBuffer, inputCompressedJpegSize, inputDecompressedJpegBuffer, 0, TJPF_RGB);
    if(funcResult < 0) {
        return tusaTJError("decompressing JPEG image");
    }

    tj3Free(inputCompressedJpegBuffer);
    tj3Destroy(tjInstance);

    // compressing
    tjInstance = tj3Init(TJINIT_COMPRESS);
    tj3Set(tjInstance, TJPARAM_SUBSAMP, inSubsamp);
    tj3Set(tjInstance, TJPARAM_QUALITY, DEFAULT_QUALITY);
    tj3Set(tjInstance, TJPARAM_FASTDCT, 1);

    tj3Compress8(tjInstance, inputDecompressedJpegBuffer, width, 0, height,  TJPF_RGB, &outputCompressedJpegBuffer, &outputCompressedJpegSize);

    outputCompressedJpegFile = fopen(outputFilePath, "wb");
    fwrite(outputCompressedJpegBuffer, outputCompressedJpegSize, 1, outputCompressedJpegFile);
    fclose(outputCompressedJpegFile);
    tj3Free(outputCompressedJpegBuffer);
    return retval;
}


