package com.example.tusa_android.stream

interface OnStreamReadListener {
    fun updateCurrentProgress(progress: Double)
    fun error(code: Int)
    fun ready()
}