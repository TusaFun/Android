package com.example.tusa_android.file

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import java.io.File

class ImageFileCompress(private val fileUri: Uri, private val context: Context) {
    suspend fun compress(quality: Int) : Uri {
        val compressedImageFile = Compressor.compress(context, File(fileUri.path!!)){
            quality(quality) // combine with compressor constraint
            format(Bitmap.CompressFormat.JPEG)
        }
        val result = Uri.fromFile(compressedImageFile)
        return result
    }
}