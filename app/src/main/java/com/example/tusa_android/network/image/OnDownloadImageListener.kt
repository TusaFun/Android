package com.example.tusa_android.network.image

interface OnDownloadImageListener {

    fun updateCurrentProgress(progress: Double)

    fun error(code: Int)
}