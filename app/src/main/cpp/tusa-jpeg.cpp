
#include "tusa-jpeg.h"

#include <android/log.h>

#ifdef _MSC_VER
#define _CRT_SECURE_NO_DEPRECATE
#endif

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cerrno>
#include <cassert>
#include "turbojpeg.h"
#include "libexif/exif-data.h"
#include "libexif/exif-loader.h"


#ifdef _WIN32
#define strcasecmp  stricmp
#define strncasecmp  strnicmp
#endif




#define THROW_UNIX(action)  THROW(action, strerror(errno))

#define DEFAULT_SUBSAMP  TJSAMP_444
#define DEFAULT_QUALITY  75

#define FILE_BYTE_ORDER EXIF_BYTE_ORDER_MOTOROLA
#define FILE_COMMENT "libexif demonstration image"
/* special header required for EXIF_TAG_USER_COMMENT */
#define ASCII_COMMENT "ASCII\0\0\0"

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

const unsigned char exif_header[] = {
        0xff, 0xd8, 0xff, 0xe1
};

/* length of data in exif_header */
const unsigned int exif_header_len = sizeof(exif_header);


ExifEntry *init_tag(ExifData *exif, ExifIfd ifd, ExifTag tag)
{
    ExifEntry *entry;
    /* Return an existing tag if one exists */
    if (!((entry = exif_content_get_entry (exif->ifd[ifd], tag)))) {
        /* Allocate a new entry */
        entry = exif_entry_new ();
        assert(entry != nullptr); /* catch an out of memory condition */
        entry->tag = tag; /* tag must be set before calling
				 exif_content_add_entry */

        /* Attach the ExifEntry to an IFD */
        exif_content_add_entry (exif->ifd[ifd], entry);

        /* Allocate memory for the entry and fill with default data */
        exif_entry_initialize (entry, tag);

        /* Ownership of the ExifEntry has now been passed to the IFD.
         * One must be very careful in accessing a structure after
         * unref'ing it; in this case, we know "entry" won't be freed
         * because the reference count was bumped when it was added to
         * the IFD.
         */
        exif_entry_unref(entry);
    }
    return entry;
}

/* Create a brand-new tag with a data field of the given length, in the
 * given IFD. This is needed when exif_entry_initialize() isn't able to create
 * this type of tag itself, or the default data length it creates isn't the
 * correct length.
 */
ExifEntry *create_tag(ExifData *exif, ExifIfd ifd, ExifTag tag, size_t len)
{
    void *buf;
    ExifEntry *entry;

    /* Create a memory allocator to manage this ExifEntry */
    ExifMem *mem = exif_mem_new_default();
    assert(mem != nullptr); /* catch an out of memory condition */

    /* Create a new ExifEntry using our allocator */
    entry = exif_entry_new_mem (mem);
    assert(entry != nullptr);

    /* Allocate memory to use for holding the tag data */
    buf = exif_mem_alloc(mem, len);
    assert(buf != nullptr);

    /* Fill in the entry */
    entry->data = static_cast<unsigned char *>(buf);
    entry->size = len;
    entry->tag = tag;
    entry->components = len;
    entry->format = EXIF_FORMAT_UNDEFINED;

    /* Attach the ExifEntry to an IFD */
    exif_content_add_entry (exif->ifd[ifd], entry);

    /* The ExifMem and ExifEntry are now owned elsewhere */
    exif_mem_unref(mem);
    exif_entry_unref(entry);

    return entry;
}

void trim_spaces(char *buf)
{
    char *s = buf-1;
    for (; *buf; ++buf) {
        if (*buf != ' ')
            s = buf;
    }
    *++s = 0; /* nul terminate the string on the first of the final spaces */
}

/* Show the tag name and contents if the tag exists */
void show_tag(ExifData *d, ExifIfd ifd, ExifTag tag)
{
    /* See if this tag exists */
    ExifEntry *entry = exif_content_get_entry(d->ifd[ifd],tag);
    if (entry) {
        char buf[1024];

        /* Get the contents of the tag in human-readable form */
        exif_entry_get_value(entry, buf, sizeof(buf));

        /* Don't bother printing it if it's entirely blank */
        trim_spaces(buf);
        if (*buf) {
            __android_log_print(ANDROID_LOG_DEBUG, "tusa-jpeg", "%s: %s\n", exif_tag_get_name_in_ifd(tag,ifd), buf);
        }
    }
}


unsigned char* simpleCompress(unsigned char* inputCompressedJpegBuffer, const int inputImageBufferSize, int* outputBufferImageSize, int quality) {
    int funcResult = 0;
    int fastUpsample = 0, fastDCT = 0;
    int width, height, inSubsamp, inColorspace;
    unsigned int exif_data_len;
    size_t inputCompressedJpegSize;
    unsigned char *inputDecompressedJpegBuffer = nullptr, *outputCompressedJpegBuffer = nullptr;
    size_t outputCompressedJpegSize;
    ExifLoader* exifLoader;
    ExifData* exifDataInput;


    // ExifLoader используется для загрузки из файла exif информации
    exifLoader = exif_loader_new();
    if(exifLoader) {
        // Это вся exif информация, которая была найдена в изображении
        //exif_loader_write_file(exifLoader, inputFilePath);
        exif_loader_write(exifLoader, inputCompressedJpegBuffer, inputImageBufferSize);
        exifDataInput = exif_loader_get_data(exifLoader);
        exif_loader_unref(exifLoader);
    }

    inputCompressedJpegSize = inputImageBufferSize;

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
        tusaTJError("decompressing JPEG image");
        return nullptr;
    }

    tj3Destroy(tjInstance);

    // compressing
    tjInstance = tj3Init(TJINIT_COMPRESS);
    tj3Set(tjInstance, TJPARAM_SUBSAMP, inSubsamp);
    tj3Set(tjInstance, TJPARAM_QUALITY, quality);
    tj3Set(tjInstance, TJPARAM_FASTDCT, 1);

    tj3Compress8(tjInstance, inputDecompressedJpegBuffer, width, 0, height,  TJPF_RGB, &outputCompressedJpegBuffer, &outputCompressedJpegSize);

    // created exif data
    unsigned char *exif_data;
    ExifEntry *entry;
    ExifData *exif = exif_data_new();

    exif_data_set_option(exif, EXIF_DATA_OPTION_FOLLOW_SPECIFICATION);
    exif_data_set_data_type(exif, EXIF_DATA_TYPE_COMPRESSED);
    exif_data_set_byte_order(exif, FILE_BYTE_ORDER);
    exif_data_fix(exif); // Create the mandatory EXIF fields with default data
    entry = init_tag(exif, EXIF_IFD_EXIF, EXIF_TAG_PIXEL_X_DIMENSION);
    exif_set_long(entry->data, FILE_BYTE_ORDER, width);
    entry = init_tag(exif, EXIF_IFD_EXIF, EXIF_TAG_PIXEL_Y_DIMENSION);
    exif_set_long(entry->data, FILE_BYTE_ORDER, height);
    entry = init_tag(exif, EXIF_IFD_EXIF, EXIF_TAG_COLOR_SPACE);
    exif_set_short(entry->data, FILE_BYTE_ORDER, 1);
    entry = create_tag(exif, EXIF_IFD_EXIF, EXIF_TAG_USER_COMMENT, sizeof(ASCII_COMMENT) + sizeof(FILE_COMMENT) - 2);

    /* Write the special header needed for a comment tag */
    memcpy(entry->data, ASCII_COMMENT, sizeof(ASCII_COMMENT)-1);
    /* Write the actual comment text, without the trailing NUL character */
    memcpy(entry->data+8, FILE_COMMENT, sizeof(FILE_COMMENT)-1);
    entry = create_tag(exif, EXIF_IFD_EXIF, EXIF_TAG_SUBJECT_AREA, 4 * exif_format_get_size(EXIF_FORMAT_SHORT));
    entry->format = EXIF_FORMAT_SHORT;
    entry->components = 4;
    exif_set_short(entry->data, FILE_BYTE_ORDER, width / 2);
    exif_set_short(entry->data+2, FILE_BYTE_ORDER, height / 2);
    exif_set_short(entry->data+4, FILE_BYTE_ORDER, width);
    exif_set_short(entry->data+6, FILE_BYTE_ORDER, height);

    auto inputExifByteOrder = exif_data_get_byte_order(exifDataInput);
    ExifEntry *inputOrientationEntry = exif_content_get_entry(exifDataInput->ifd[EXIF_IFD_0], EXIF_TAG_ORIENTATION);
    short orientation = exif_get_short(inputOrientationEntry->data, inputExifByteOrder);
    entry = init_tag(exif, EXIF_IFD_EXIF, EXIF_TAG_ORIENTATION);
    exif_set_short(entry->data, FILE_BYTE_ORDER, orientation);

    exif_data_save_data(exif, &exif_data, &exif_data_len);
    size_t skipOutputCompressedJpeg = 20;

    *outputBufferImageSize = exif_header_len + 2 + exif_data_len + outputCompressedJpegSize - skipOutputCompressedJpeg;

    // it must live It is return value
    auto outputBufferImage = (unsigned char*) malloc(*outputBufferImageSize + 1);
    memcpy(outputBufferImage, exif_header, exif_header_len);
    outputBufferImage[exif_header_len] = (exif_data_len+2) >> 8;
    outputBufferImage[exif_header_len + 1] = (exif_data_len + 2) & 0xff;
    memcpy(outputBufferImage + exif_header_len + 2, exif_data, exif_data_len);
    memcpy(outputBufferImage + exif_header_len + 2 + exif_data_len, outputCompressedJpegBuffer + skipOutputCompressedJpeg, outputCompressedJpegSize - skipOutputCompressedJpeg);
    outputBufferImage[*outputBufferImageSize] = '\n';


    tj3Destroy(tjInstance);
    tj3Free(outputCompressedJpegBuffer);
    exif_data_unref(exif);

    return outputBufferImage;
}


