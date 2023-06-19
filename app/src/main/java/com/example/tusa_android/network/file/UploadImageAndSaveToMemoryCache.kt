package com.example.tusa_android.network.file

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.tusa_android.cache.MemoryBitmapsCache

class UploadImageAndSaveToMemoryCache(
    private val resultCompressedImage: Uri,
    private val context: Context,
    private val tusaFileType: String
    ) {

    fun start() : String{
        val resultUri = resultCompressedImage
        val inputStream = context.contentResolver.openInputStream(resultUri)!!
        val bytes = inputStream.readBytes()
        inputStream.close()

        val uploadFile = UploadImage(bytes, tusaFileType)
        val path = uploadFile.start()

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        MemoryBitmapsCache.getInstance().saveBitmap(path, bitmap)
        return path
    }
}