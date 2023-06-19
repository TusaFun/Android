package com.example.tusa_android.stream

import java.io.InputStream

class StreamReaderWithProgress(
    val inputStream: InputStream,
    private val contentLength: Int,
    private val listener: OnStreamReadListener
    ) {

    fun read() : ByteArray{
        val length = contentLength
        val blob = ByteArray(length)
        var bytesRead = 0
        while (bytesRead < length) {
            val n: Int = inputStream.read(blob, bytesRead, length - bytesRead)
            if(n <= 0) {
                // error
                listener.error(n)
            }
            bytesRead += n

            val progress = bytesRead.toDouble().div(length)
            listener.updateCurrentProgress(progress)
        }

        listener.ready()
        return blob
    }
}