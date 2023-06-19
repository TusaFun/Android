package com.example.tusa_android.map.markers.quad_tree

interface QuadTreeContainsPointCoordinate {

    fun contains(point: QuadTreePoint) : Boolean
    fun intersects(range: QuadTreeRectangle): Boolean

}