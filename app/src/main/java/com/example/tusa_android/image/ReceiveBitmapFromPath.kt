package com.example.tusa_android.image

import android.graphics.Bitmap
import com.example.tusa_android.cache.MemoryBitmapsCache
import com.example.tusa_android.network.AppUrl
import com.example.tusa_android.network.image.DownloadImageAndSaveToMemoryCache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class ReceiveBitmapFromPath(val onProgress: (value: Int) -> Unit) {

    fun setupImageUseTryUseMemoryCache(path: String): Bitmap? {
        val bitmap = MemoryBitmapsCache.getInstance().loadBitmap(path)
        if (bitmap != null) {
            return bitmap
        }

        return loadUrlAndImage(path)
    }

    private fun loadUrlAndImage(path: String): Bitmap? {
        var usePath = path


        val client = OkHttpClient().newBuilder().build()
        val request: Request = Request.Builder()
            .url("${AppUrl.filesUrl}api/files/download-url?path=${usePath}")
            .build()
        val response: Response = client.newCall(request).execute()
        val downloadUrl = response.body!!.string()
        println(downloadUrl)

        return loadImageFromUrl(usePath, downloadUrl)
    }

    private fun loadImageFromUrl(path: String, url: String): Bitmap? {
        val downloadImageAndSaveToMemoryCache = DownloadImageAndSaveToMemoryCache(path, url) {
            onProgress.invoke(it)
        }
        return downloadImageAndSaveToMemoryCache.download()
    }

}