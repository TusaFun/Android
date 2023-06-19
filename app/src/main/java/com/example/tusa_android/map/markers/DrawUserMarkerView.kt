package com.example.tusa_android.map.markers

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.tusa_android.image.ReceiveBitmapFromPath
import com.mapbox.bindgen.Value

class DrawUserMarkerView : View {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private var radius: Int = 0
    private var bitmap: Bitmap? = null

    fun drawBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate()
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener {
            val newValue = it.animatedValue as Int
            radius = newValue
            invalidate()
        })
        animator.start()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cw = width / 2
        val ch = height / 2

        val paint = Paint()
        paint.color = Color.RED

        canvas.drawCircle(cw.toFloat(), ch.toFloat(), radius.toFloat(), paint)

        if(bitmap != null) {
            canvas.drawBitmap(bitmap!!, 0F, 50F, paint)
        }
    }
}