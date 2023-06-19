package com.example.tusa_android.map.markers

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.tusa_android.R


class MarkerTriangleView : View {
    private lateinit var paint: Paint
    private var path: Path = Path()
    private var rectF = RectF(0f, 0f, 500f, 300f)

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

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val color = ContextCompat.getColor(context, R.color.md_theme_light_primary)
        paint = Paint()
        paint.color = color
        paint.strokeWidth = 4f

        rectF = RectF(0f, -200f, 200f, 300f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        rectF.top = contentHeight.toFloat() * -1;
        rectF.right = contentWidth.toFloat()
        rectF.bottom = contentHeight.toFloat()
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawArc(rectF, 0f, 180f, true, paint)
    }
}