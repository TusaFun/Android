package com.example.tusa_android.map.markers.quad_tree

class QuadTreeRectangle(var x: Double, var y: Double, var w: Int, var h : Int, divideBounds: Boolean = true) : QuadTreeContainsPointCoordinate {
    init {
        if(divideBounds) {
            this.w = this.w / 2
            this.h = this.h / 2
        }
    }

    var debugTransparent = false;

    override fun contains(point: QuadTreePoint) : Boolean {
        return (point.x >= this.x - this.w &&
                point.x <= this.x + this.w &&
                point.y >= this.y - this.h &&
                point.y <= this.y + this.h);
    }

    override fun intersects(range: QuadTreeRectangle): Boolean {
        return !(range.x - range.w > this.x + this.w ||
                range.x + range.w < this.x - this.w ||
                range.y - range.h > this.y + this.h ||
                range.y + range.h < this.y - this.h);
    }
}