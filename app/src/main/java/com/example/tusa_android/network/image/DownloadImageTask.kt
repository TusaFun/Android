package com.example.tusa_android.network.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.tusa_android.cache.MemoryBitmapsCache
import com.example.tusa_android.stream.OnStreamReadListener
import com.example.tusa_android.stream.StreamReaderWithProgress
import java.util.concurrent.Callable


class DownloadImageTask(val input: DownloadImageInput) : Callable<Bitmap?> {

    override fun call(): Bitmap? {
        try {
            val connection = java.net.URL(input.url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val imageLength = connection.contentLength
            val reader = StreamReaderWithProgress(inputStream, imageLength, object : OnStreamReadListener {
                override fun updateCurrentProgress(progress: Double) {
                    input.onProgress.updateCurrentProgress(progress)
                }
                override fun error(code: Int) {
                    input.onProgress.error(code)
                }
                override fun ready() {
                    inputStream.close()
                }
            })
            val imageBlob = reader.read()
            val imageBitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageLength)

            return imageBitmap
        } catch (exception: Exception) {
            println(exception.message)
        }

        return  null
    }
}