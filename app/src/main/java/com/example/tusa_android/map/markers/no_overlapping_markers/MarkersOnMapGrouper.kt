package com.example.tusa_android.map.markers.no_overlapping_markers
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.tusa_android.TusaMarker
import com.example.tusa_android.map.markers.DebugMarkersDrawView
import com.example.tusa_android.map.markers.UserMarkerView
import com.example.tusa_android.map.markers.quad_tree.QuadTree
import com.example.tusa_android.map.markers.quad_tree.QuadTreeCircle
import com.example.tusa_android.map.markers.quad_tree.QuadTreePoint
import com.example.tusa_android.map.markers.quad_tree.QuadTreeRectangle
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnScaleListener
import com.mapbox.maps.plugin.gestures.gestures
import kotlin.math.*

class MarkersOnMapGrouper(private val mapView: MapView, private val debugMarkersDrawView: DebugMarkersDrawView, private val context: Context) {
    private val userMarkersViewMap: MutableMap<String, UserMarkerView> = mutableMapOf<String, UserMarkerView>()
    private val viewAnnotationManager get() = mapView.viewAnnotationManager
    private var zoom: Double = 0.0
    private var lastMarkersSize = 200
    private var markers: List<TusaMarker> = arrayListOf()

    private val onScaleListener = object : OnScaleListener {
        override fun onScale(detector: StandardScaleGestureDetector) {

        }
        override fun onScaleBegin(detector: StandardScaleGestureDetector) {

        }
        override fun onScaleEnd(detector: StandardScaleGestureDetector) {
            val currentZoom = mapView.getMapboxMap().cameraState.zoom
            zoom = currentZoom
        }
    }

    init {
        mapView.gestures.addOnScaleListener(onScaleListener)
    }

    fun destroy() {
        mapView.gestures.removeOnScaleListener(onScaleListener)
    }



    fun handleMarkers(markers: MutableList<TusaMarker>) {
        this.markers = markers
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val markerSize = 150 // стандартный размер
            val markerRadius = markerSize / 2
            val minimumMarkerRadius = 40.0 // минимальный размер маркера при изменении размера
            val maxOffsetDistance = 150.0 // насколько далеко маркер может быть отодвинут Иначе скрыть
            lastMarkersSize = markerSize

            // Создает новые маркеры
            for(marker in markers) {
                val createdOnMapMarker = userMarkersViewMap[marker.username]
                if(createdOnMapMarker == null) {
                    val userMarker = createAndAddViewAnnotation(marker, markerSize)
                    userMarkersViewMap[marker.username] = userMarker
                }
            }

            // Создаем quad tree
            val mapHeight = mapView.height
            val mapWidth = mapView.width
            val bounds = QuadTreeRectangle(mapWidth / 2.0, mapHeight / 2.0, mapWidth, mapHeight)
            val points = mutableListOf<QuadTreePoint>()
            val quadTree = QuadTree(bounds, 1)

            debugMarkersDrawView.clear()

            // собираем информацию о расположении маркеров на карте
            // берем координаты на экране
            val markersViews = userMarkersViewMap.values.toList()
            for(marker in markersViews) {
                // зачищаем предыдущие, назначенные оффсеты
                marker.resetOffset()
                val screenCoordinate = mapView.getMapboxMap().pixelForCoordinate(marker.point)
                val offsetY = marker.currentOffsetY
                val offsetX = marker.currentOffsetX
                val point = QuadTreePoint(
                    QuadTreeCircle(screenCoordinate.x + offsetX, screenCoordinate.y - offsetY,
                        markerRadius.toDouble(),
                    ),
                    marker
                )
                //debugMarkersDrawView.addRectangle(point.form, false)
                points.add(point)
                quadTree.insert(point)
            }

            // перемешать маркера для рандомного уменьшения или увеличения маркеров
            // без этого последний маркер в листе всегда будет увеличенным
            // а первые всегда будут уменьшенными
            points.shuffle()

            // повторяем 3 раза Так достигается максимально рациональное расположение маркеров
            for(i in 0..3) {
                for(point in points) {
                    // зона в которой будем искать ближайшие маркера
                    val circleRange = QuadTreeCircle(point.x, point.y, point.getRadius() * 2)
                    val pointsInRange = mutableListOf<QuadTreePoint>();
                    quadTree.query(circleRange, pointsInRange)
                    for(pointQueried in pointsInRange) {
                        if(point.markerView == pointQueried.markerView) {
                            continue
                        }
                        var intersection = point.intersects(pointQueried)
                        // если пересекаются то
                        if(intersection > 0) {
                            // вычисляем вектор градиента от маркера с которым пересекаемся
                            val gradientX = point.x - pointQueried.x
                            val gradientY = point.y - pointQueried.y

                            // это дистанция на которой ценрты удалены друг от друга
                            val gradLength = sqrt(gradientX * gradientX + gradientY * gradientY)

                            if(gradLength <= 80) {
                                // Если маркера наложены друг на друга то уменьшаем размер и сдвигаем маркер
                                point.newRadius = point.getRadius() / 2
                            } else if(intersection < 80 && point.getRadius() > 60) {
                                // Если пересечение совсем небольшое то уменьшаем маркер на это значение
                                point.newRadius = point.getRadius() - intersection
                            }

                            // в любом исходе маркер не должен быть меньше минимального размера
                            if(point.newRadius != null) {
                                point.newRadius = max(point.newRadius!!, minimumMarkerRadius)
                            }

                            // пересчитываем пересечение
                            intersection = point.intersects(pointQueried)

                            // вычисляем угол отдвигания маркера
                            var angle = atan(gradientY / gradientX) * -1
                            if (gradientX < 0 && gradientY > 0) {
                                angle += PI
                            } else if (gradientX < 0 && gradientY < 0) {
                                angle -= PI
                            }
                            val x = kotlin.math.cos(angle) * intersection;
                            val y = kotlin.math.sin(angle) * intersection;
                            point.offsetX += x.toInt()
                            point.offsetY += y.toInt()
                        }
                    }
                }
            }


            // скрываем все маркера, которые маленькие и пересекаются
            for(point in points) {

                // если у маркера слишком сильный offset то скрываем его
                val offsetDistance = sqrt(point.offsetX.toDouble() * point.offsetX + point.offsetY * point.offsetY)
                if(offsetDistance > maxOffsetDistance) {
                    point.hide = true
                    continue
                }

                val circleRange = QuadTreeCircle(point.x, point.y, point.getRadius() * 2)
                val pointsInRange = mutableListOf<QuadTreePoint>();
                quadTree.query(circleRange, pointsInRange)
                for(pointQueried in pointsInRange) {
                    if(point.markerView == pointQueried.markerView) {
                        continue
                    }

                    // проверяем пересечение
                    val intersection = point.intersects(pointQueried)
                    if(intersection > 30) {
                        // если больше 30 то скрываем маркер
                        if(point.getRadius().toInt() < markerRadius) {
                            point.hide = true
                            break
                        }
                    }
                }
            }


            for(point in points) {
                var x = point.x
                var y = point.y
                if(point.hide) {
                    point.markerView.setOffsetXYAndSize(point.offsetX, point.offsetY, 1)
                    continue
                }
                point.markerView.setOffsetXYAndSize(point.offsetX, point.offsetY, point.getRadius().toInt() * 2)
            }

            debugMarkersDrawView.draw()
        }
    }

    private fun createAndAddViewAnnotation(tusaMarker: TusaMarker, size: Int) : UserMarkerView {
        val userMarkerView = UserMarkerView(context, mapView)
        val view = userMarkerView.createAndAddView(tusaMarker, size)
        return userMarkerView
    }

}