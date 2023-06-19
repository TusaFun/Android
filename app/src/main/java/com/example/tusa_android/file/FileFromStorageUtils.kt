package com.example.tusa_android.file

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.*

class FileFromStorageUtils(val context: Context, private val uri: Uri) {

    @Throws(IOException::class)
    fun getFilePathFromUri(): Uri {
        val fileName: String = getFileName()
        val file = File(context.externalCacheDir, fileName)
        file.createNewFile()
        FileOutputStream(file).use { outputStream ->
            context.contentResolver?.openInputStream(uri).use { inputStream ->
                copyFile(inputStream, outputStream)
                outputStream.flush()
            }
        }
        return Uri.fromFile(file)
    }

    fun getFileName(): String {
        var fileName: String? = getFileNameFromCursor()
        if (fileName == null) {
            val fileExtension: String? = getFileExtension()
            fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
        } else if (!fileName.contains(".")) {
            val fileExtension: String? = getFileExtension()
            fileName = "$fileName.$fileExtension"
        }
        return fileName
    }

    fun getFileExtension(): String? {
        val fileType: String? = context.contentResolver?.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream?, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (`in`?.read(buffer).also { read = it!! } != -1) {
            read?.let { out.write(buffer, 0, it) }
        }
    }

    private fun getFileNameFromCursor(): String? {
        val fileCursor: Cursor? = context.contentResolver
            ?.query(uri, arrayOf<String>(OpenableColumns.DISPLAY_NAME), null, null, null)
        var fileName: String? = null
        if (fileCursor != null && fileCursor.moveToFirst()) {
            val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cIndex != -1) {
                fileName = fileCursor.getString(cIndex)
            }
        }
        return fileName
    }
}