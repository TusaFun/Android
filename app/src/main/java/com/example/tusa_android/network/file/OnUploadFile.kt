package com.example.tusa_android.network.file

interface OnUploadFile {
    fun onProgress(progress: Double)
    fun onError()
    fun onCompleted()
}