package com.example.tusa_android.network.image

import android.graphics.Bitmap
import com.example.tusa_android.cache.MemoryBitmapsCache

class DownloadImageAndSaveToMemoryCache(
    val path: String,
    val url: String,
    val progressUpdate: (progress: Int) -> Unit
    ) {

    fun download() : Bitmap? {
        val input = DownloadImageInput(url, object : OnDownloadImageListener {
            override fun updateCurrentProgress(progress: Double) {
                val progressInt = (progress * 100).toInt()
                progressUpdate.invoke(progressInt)
            }
            override fun error(code: Int) {

            }
        })
        val result = DownloadImageTask(input).call() ?: return null
        MemoryBitmapsCache.getInstance().saveBitmap(path, result)
        return result
    }
}