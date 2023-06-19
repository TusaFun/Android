package com.example.tusa_android.map.markers

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.tusa_android.map.markers.quad_tree.QuadTreeRectangle

class DebugMarkersDrawView : View {
    private var listOfRectangles : MutableList<Pair<QuadTreeRectangle, Rect>> = mutableListOf()
    private var rectPaint = Paint().apply {
        this.color = Color.BLACK
        this.strokeWidth = 40f
    }

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

    }

    fun clear() {
        listOfRectangles.clear()
    }

    fun addRectangle(rectangle: QuadTreeRectangle, transparent: Boolean) {
        rectangle.debugTransparent = transparent
        val left = rectangle.x - rectangle.w;
        val right = rectangle.x + rectangle.w;
        val top = rectangle.y - rectangle.h;
        val bottom = rectangle.y + rectangle.h;
        val drawRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        listOfRectangles.add(Pair(rectangle, drawRect))
    }

    fun draw() {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        var canvasHeight = height
        var canvasWidth = width

        for(rectValue in listOfRectangles) {
            if(rectValue.first.debugTransparent) {
                rectPaint.alpha = 180
            } else {
                rectPaint.alpha = 255
            }

            rectPaint.strokeWidth  = (rectValue.first.w).toFloat()
            canvas.drawPoint(rectValue.first.x.toFloat(), rectValue.first.y.toFloat(), rectPaint)
            //canvas.drawRect(rectValue.second, rectPaint)
        }
    }
}