package com.example.tusa_android.map.markers.quad_tree

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class QuadTreeCircle(var x: Double, var y: Double, var radius: Double) : QuadTreeContainsPointCoordinate{

    private var rSquared = radius * radius

    override fun contains(point: QuadTreePoint) : Boolean {
        val d = (point.x - x).pow(2.0) + (point.y - y).pow(2.0)
        return d <= rSquared
    }

    override fun intersects(rect: QuadTreeRectangle) : Boolean {
        val xDist = abs(rect.x - this.x);
        val yDist = abs(rect.y - this.y);

        // radius of the circle
        val r = this.radius;

        val w = rect.w;
        val h = rect.h;

        val edges = (xDist - w).pow(2) + (yDist - h).pow(2);

        // no intersection
        if (xDist > (r + w) || yDist > (r + h))
            return false;

        // intersection within the circle
        if (xDist <= w || yDist <= h)
            return true;

        // intersection on the edge of the circle
        return edges <= this.rSquared;
    }
}