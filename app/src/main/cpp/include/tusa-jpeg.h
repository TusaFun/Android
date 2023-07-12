
#ifndef TUSA_JPEG_H
#define TUSA_JPEG_H

int compressImage(int argc, char** argv);
unsigned char* simpleCompress(unsigned char* inputImageBuffer, const int inputImageBufferSize, int* outputBufferImageSize, int quality);

#endif //TUSA_JPEG_H