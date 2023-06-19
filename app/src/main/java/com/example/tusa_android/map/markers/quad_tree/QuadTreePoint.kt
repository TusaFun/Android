package com.example.tusa_android.map.markers.quad_tree

import com.example.tusa_android.map.markers.UserMarkerView
import kotlin.math.pow
import kotlin.math.sqrt

class QuadTreePoint(var form: QuadTreeCircle, var markerView: UserMarkerView) {
    var intersects = false
    var hide = false;
    var fixPosition = false;
    var listOfIntersections: MutableList<QuadTreePoint> = mutableListOf()

    val x: Double get() = form.x + offsetX
    val y: Double get() = form.y - offsetY

    fun intersects(otherPoint: QuadTreePoint) : Double {
        val distance = sqrt((x - otherPoint.x).pow(2) + (y - otherPoint.y).pow(2))
        return (otherPoint.getRadius() + getRadius()) - distance + 5
        // if negative => no intersections
        // if positive => has intersections
    }

    fun getRadius(): Double {
        if(newRadius != null) {
            return newRadius as Double
        }

        return form.radius
    }

    var offsetY: Int = 0
    var offsetX: Int = 0
    var newRadius: Double? = null
}