package com.example.tusa_android.map.markers.quad_tree

class QuadTree(var boundary: QuadTreeRectangle, var capacity: Int) {
    private var divided : Boolean = false
    private var points: MutableList<QuadTreePoint> = arrayListOf()
    private var northeast: QuadTree? = null
    private var northwest: QuadTree? = null
    private var southeast: QuadTree? = null
    private var southwest: QuadTree? = null

    fun subdivide() {
        val x = boundary.x
        val y = boundary.y
        val w = boundary.w / 2
        val h = boundary.h / 2

        val ne = QuadTreeRectangle(x + w, y - h, boundary.w, boundary.h)
        this.northeast = QuadTree(ne, this.capacity)
        val nw = QuadTreeRectangle(x - w, y - h, boundary.w, boundary.h)
        this.northwest = QuadTree(nw, this.capacity)
        val se = QuadTreeRectangle(x + w, y + h, boundary.w, boundary.h)
        this.southeast = QuadTree(se, this.capacity)
        val sw = QuadTreeRectangle(x - w, y + h, boundary.w, boundary.h)
        this.southwest = QuadTree(sw, this.capacity)

        divided = true
    }

    fun insert(point: QuadTreePoint) : Boolean{
        if (!this.boundary.contains(point)) {
            return false
        }

        if (this.points.count() < this.capacity) {
            this.points.add(point)
            return true;
        }

        if (!this.divided) {
            this.subdivide();
        }

        if (this.northeast?.insert(point) == true || this.northwest?.insert(point) == true ||
            this.southeast?.insert(point) == true || this.southwest?.insert(point) == true) {
            return true;
        }

        return false;
    }

    fun query(range: QuadTreeContainsPointCoordinate, found: MutableList<QuadTreePoint>) {
        if (!range.intersects(this.boundary)) {
            return
        }
        for (p in this.points) {
            if (range.contains(p)) {
                found.add(p)
            }
        }
        if (this.divided) {
            this.northwest?.query(range, found);
            this.northeast?.query(range, found);
            this.southwest?.query(range, found);
            this.southeast?.query(range, found);
        }
    }
}