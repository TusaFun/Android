package com.example.tusa_android.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.tusa_android.R
import com.example.tusa_android.cache.MemoryBitmapsCache
import com.example.tusa_android.network.AppUrl
import com.example.tusa_android.network.image.DownloadImageAndSaveToMemoryCache
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TusaImageView(context: Context, attributeSet: AttributeSet)
    : FrameLayout(context, attributeSet) {

    private val _executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var _imageView: ImageView
    private var _progressBar: CircularProgressIndicator

    fun setUri(uri: Uri) {
        _imageView.setImageURI(uri)
    }

    fun setBitmap(bitmap: Bitmap) {
        _imageView.setImageBitmap(bitmap)
    }

    fun setupImageUseTryUseMemoryCache(path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val handler = Handler(Looper.getMainLooper())
                val receiveBitmapFromPath = ReceiveBitmapFromPath() {
                    handler.post {
                        _progressBar.setProgress(it, true)
                    }
                }

                val bitmap = receiveBitmapFromPath.setupImageUseTryUseMemoryCache(path)
                handler.post {
                    _imageView.setImageBitmap(bitmap)
                    _progressBar.hide()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

        }
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.sample_tusa_image_view, this)
        _imageView = view.findViewById<ImageView>(R.id.imageView)
        _progressBar = view.findViewById<CircularProgressIndicator>(R.id.progressBar)
    }
}