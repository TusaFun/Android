package com.example.tusa_android.network

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.tusa_android.R
import com.google.android.material.progressindicator.LinearProgressIndicator


class UploadProgressView : FrameLayout {

    private var _startTextView: TextView
    private lateinit var _startText: String
    private lateinit var _progressBarView: LinearProgressIndicator

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
    }

    constructor(context: Context) : super(context){
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.sample_upload_progress_view, this)
        _startTextView = view.findViewById<TextView>(R.id.infoText)
        _progressBarView = view.findViewById<LinearProgressIndicator>(R.id.progressBar)
    }

    fun setText(text: String) {
        _startTextView.text = text
    }

    fun setProgress() {

    }

}