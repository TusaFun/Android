package com.example.tusa_android.map.markers

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.example.tusa_android.R
import com.example.tusa_android.TusaMarker
import com.example.tusa_android.databinding.SampleUserMarkerViewBinding
import com.example.tusa_android.image.TusaImageView
import com.example.tusa_android.profile.AnyProfile
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.viewannotation.viewAnnotationOptions

class UserMarkerView : FrameLayout {

    val currentOffsetX: Int get() = offsetX
    val currentOffsetY: Int get() = offsetY

    var radius: Double = 0.0;

    val sizeOfMarker: Int get() = size
    val username: String get() = tusaMarker.username
    val point: com.mapbox.geojson.Point get() = com.mapbox.geojson.Point.fromLngLat(tusaMarker.longitude, tusaMarker.latitude)!!

    private lateinit var mapView: MapView
    private lateinit var tusaMarker: TusaMarker
    private lateinit var markerView: View
    private var markerBreathAnimation: Animation? = null
    private var size: Int = 0
    private var offsetX: Int = 0
    private var offsetY: Int = 0
    private var lastOffsetX: Int = offsetX
    private var lastOffsetY: Int = offsetY
    private var lastSizeOfMarker: Int = sizeOfMarker


    private val viewAnnotationManager get() = mapView.viewAnnotationManager

    constructor(context: Context, mapView: MapView) : super(context) {
        this.mapView = mapView
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



    fun updateTusaMarker(tusaMarker: TusaMarker, visible: Boolean) {
        if(this.tusaMarker.username != tusaMarker.username) {
            throw java.lang.Exception("Marker's usernames doesn't matches! updateTusaMarker")
        }

        if(this.tusaMarker.latitude == tusaMarker.latitude && this.tusaMarker.longitude == tusaMarker.longitude) {
            this.tusaMarker = tusaMarker
            return
        }

        val fromLat = this.tusaMarker.latitude
        val fromLon = this.tusaMarker.longitude

        val propertyValueHolderLat = PropertyValuesHolder.ofFloat("lat", fromLat.toFloat(), tusaMarker.latitude.toFloat())
        val propertyValueHolderLon = PropertyValuesHolder.ofFloat("lon", fromLon.toFloat(), tusaMarker.longitude.toFloat())

        val moveLatAnimation = ValueAnimator.ofPropertyValuesHolder(propertyValueHolderLat, propertyValueHolderLon)
        moveLatAnimation.addUpdateListener {
            val lat = it.getAnimatedValue("lat") as Float
            val lon = it.getAnimatedValue("lon") as Float
            val point = Point.fromLngLat(lon.toDouble(), lat.toDouble())

            viewAnnotationManager.updateViewAnnotation(markerView,
                viewAnnotationOptions {
                    geometry(point)
                    width(size)
                    height(size)
                    offsetY(offsetY)
                    offsetX(offsetX)
                    visible(visible)
                    allowOverlap(true)
                }
            )
        }
        moveLatAnimation.start()


        this.tusaMarker = tusaMarker
    }

    fun setOffsetXYAndSize(newOffsetX: Int, newOffsetY: Int, newSizeSet: Int) {
        val propertyValueHolderOffsetX = PropertyValuesHolder.ofInt("offsetX", lastOffsetX, newOffsetX.toInt())
        val propertyValueHolderOffsetY = PropertyValuesHolder.ofInt("offsetY", lastOffsetY, newOffsetY.toInt())
        val propertyValueHolderOffsetSize = PropertyValuesHolder.ofInt("size", lastSizeOfMarker, newSizeSet ?: sizeOfMarker)

        val moveOffsetAnimation = ValueAnimator.ofPropertyValuesHolder(
            propertyValueHolderOffsetX, propertyValueHolderOffsetY, propertyValueHolderOffsetSize
        )
        moveOffsetAnimation.addUpdateListener {
            val offsetX = it.getAnimatedValue("offsetX") as Int
            val offsetY = it.getAnimatedValue("offsetY") as Int
            val newSize = it.getAnimatedValue("size") as Int

            lastOffsetX = offsetX
            lastOffsetY = offsetY
            lastSizeOfMarker = newSize

            markerView.layoutParams = FrameLayout.LayoutParams(newSize, newSize)
            viewAnnotationManager.updateViewAnnotation(markerView,
                viewAnnotationOptions {
                    geometry(point)
                    allowOverlap(true)
                    visible(true)
                    width(newSize)
                    height(newSize)
                    offsetX(offsetX)
                    offsetY(offsetY)
                }
            )
        }
        moveOffsetAnimation.start()

        this.size = newSizeSet
        offsetX = x.toInt()
        offsetY = y.toInt()
    }

    fun setVisibility(state: Boolean) {
        viewAnnotationManager.updateViewAnnotation(markerView,
            viewAnnotationOptions {
                geometry(point)
                visible(state)
            }
        )
    }

    fun setAngleAndDistance(angle: Double, distance: Int, size: Int?) {
        this.radius = distance.toDouble()
        val x = kotlin.math.cos(angle) * distance;
        val y = kotlin.math.sin(angle) * distance;

        val propertyValueHolderOffsetX = PropertyValuesHolder.ofInt("offsetX", lastOffsetX, x.toInt())
        val propertyValueHolderOffsetY = PropertyValuesHolder.ofInt("offsetY", lastOffsetY, y.toInt())
        val propertyValueHolderOffsetSize = PropertyValuesHolder.ofInt("size", lastSizeOfMarker, size ?: sizeOfMarker)

        val moveOffsetAnimation = ValueAnimator.ofPropertyValuesHolder(
            propertyValueHolderOffsetX, propertyValueHolderOffsetY, propertyValueHolderOffsetSize
        )
        moveOffsetAnimation.addUpdateListener {
            val offsetX = it.getAnimatedValue("offsetX") as Int
            val offsetY = it.getAnimatedValue("offsetY") as Int
            val newSize = it.getAnimatedValue("size") as Int
            lastOffsetX = offsetX
            lastOffsetY = offsetY
            lastSizeOfMarker = newSize
            markerView.layoutParams = FrameLayout.LayoutParams(newSize, newSize)
            viewAnnotationManager.updateViewAnnotation(markerView,
                viewAnnotationOptions {
                    geometry(point)
                    allowOverlap(true)
                    width(newSize)
                    height(newSize)
                    offsetX(offsetX)
                    offsetY(offsetY)
                }
            )
        }
        moveOffsetAnimation.start()
        if(size != null) {
            this.size = size
        }

        offsetX = x.toInt()
        offsetY = y.toInt()
    }

    fun resetOffset() {
        radius = 0.0
        val distance = 0
        setAngleAndDistance(radius, distance, null)
    }

    fun createAndAddView(tusaMarker: TusaMarker, size: Int) : View{
        markerBreathAnimation = AnimationUtils.loadAnimation(context, R.anim.common_animation)
        this.size = size
        val view = inflate(context, R.layout.sample_user_marker_view, this)
        view.layoutParams = FrameLayout.LayoutParams(size, size)
        this.tusaMarker = tusaMarker

        val tusaImageView = view.findViewById<TusaImageView>(R.id.imageMarkerView)
        val pathToAvatar = AnyProfile.makePathToAvatar(tusaMarker.username)
        tusaImageView.setupImageUseTryUseMemoryCache(pathToAvatar)
        radius = 0.0
        val distance = 0
        val x = kotlin.math.cos(radius) * distance;
        val y = kotlin.math.sin(radius) * distance;
        offsetX = x.toInt()
        offsetY = y.toInt()
        viewAnnotationManager.addViewAnnotation(
            view,
            viewAnnotationOptions {
                geometry(point)
                width(size)
                height(size)
                offsetY(offsetY)
                offsetX(offsetX)
                allowOverlap(true)
            }
        )
        SampleUserMarkerViewBinding.bind(view)
        markerView = view
        return view
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}