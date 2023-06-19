package com.example.tusa_android.map

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import com.example.tusa_android.ShowTusaMarkersReply
import com.example.tusa_android.ShowTusaMarkersRequest
import com.example.tusa_android.TusaMarker
import com.example.tusa_android.device.DeviceInfoSender
import com.example.tusa_android.map.markers.DebugMarkersDrawView
import com.example.tusa_android.map.markers.no_overlapping_markers.MarkersOnMapGrouper
import com.example.tusa_android.network.Grpc
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.*

class MapWrapper(private val deviceInfoSender: DeviceInfoSender, private val context: Context, private val coroutineScope: CoroutineScope) {
    private var _mapView: MapView? = null
    private var _currentPoint: Point? = null
    private var loadMarkersJob: Job? = null
    private lateinit var markersOnMapGrouper: MarkersOnMapGrouper
    private var _markersLoading = false

    private var loadedTusaMarkers = mutableListOf<TusaMarker>()

    @SuppressLint("Lifecycle")
    fun onStart() {
        _mapView?.onStart()

    }

    @SuppressLint("Lifecycle")
    fun onStop() {
        _mapView?.onStop()
    }

    @SuppressLint("Lifecycle")
    fun onLowMemory() {
        _mapView?.onLowMemory()
    }

    @SuppressLint("Lifecycle")
    fun onDestroy() {
        markersOnMapGrouper.destroy()
        _mapView?.location?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        _mapView?.gestures?.removeOnMoveListener(onMoveListener)
        _mapView?.onDestroy()
    }

    fun readyMapView(mapView: MapView, debugMarkersDrawView: DebugMarkersDrawView) {
        _mapView = mapView
        markersOnMapGrouper = MarkersOnMapGrouper(mapView, debugMarkersDrawView, context)

        mapView.gestures.addOnMapClickListener {
            println("add marker to map lat ${it.latitude()} lon ${it.longitude()}")
            addMarker(it)
            return@addOnMapClickListener false
        }


        mapView.compass.updateSettings {
            this.enabled = false
        }
        mapView.scalebar.updateSettings {
            this.enabled = false
        }


        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent(true)
            setupGesturesListener()
        }

        // обновляем маркера в цикле
        coroutineScope.launch {
            delay(2_000)
            loadMarkers()
            while (true) {
                delay(2_000)
                //loadMarkers()

            }
        }

    }

    var addTestMarkerIndex = 1
    private fun addMarker(point : Point) {
        val marker = TusaMarker.newBuilder()
            .setUsername("test$addTestMarkerIndex")
            .setLatitude(point.latitude())
            .setLongitude(point.longitude())
            .build()
        loadedTusaMarkers.add(marker)
        updateMarkers()
        addTestMarkerIndex += 1
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        val point = it

        // Установка координат в самом начале
        if(_currentPoint == null) {
            val options = CameraOptions.Builder().center(point).build()
            _mapView?.getMapboxMap()?.setCamera(options)
        }

        // Обновление моих координат для отправки на сервер
        _currentPoint = point
        val latitude = point.latitude()
        val longitude = point.longitude()
        deviceInfoSender.updateMyLocation(latitude, longitude)
    }

    private fun setupGesturesListener() {
        _mapView!!.gestures.addOnMoveListener(onMoveListener)
    }


    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            coroutineScope.launch {
                //loadMarkers()
                updateMarkers()
            }
        }
    }

    private fun updateMarkers() {
        markersOnMapGrouper.handleMarkers(loadedTusaMarkers)
    }

    private fun loadMarkers() {
        if(loadMarkersJob != null && loadMarkersJob!!.isActive) {
            return
        }
        loadMarkersJob = coroutineScope.launch {
            val currentCameraState = _mapView!!.getMapboxMap().cameraState
            val cameraOptions = CameraOptions.Builder()
                .center(currentCameraState.center)
                .padding(currentCameraState.padding)
                .bearing(currentCameraState.bearing)
                .pitch(currentCameraState.pitch)
                .zoom(currentCameraState.zoom)
                .build()
            val cameraBounds = _mapView!!.getMapboxMap().coordinateBoundsForCamera(cameraOptions)
            val southWest = cameraBounds.southwest
            val northEast = cameraBounds.northeast

            val lowerLatitude = southWest.latitude()
            val lowerLongitude = southWest.longitude()
            val upperLatitude = northEast.latitude()
            val upperLongitude = northEast.longitude()

            val showTusaMarkersRequest = ShowTusaMarkersRequest.newBuilder()
                .setLowerLeftY(lowerLatitude)
                .setLowerLeftX(lowerLongitude)
                .setUpperRightY(upperLatitude)
                .setUpperRightX(upperLongitude)
                .build()

            if(!_markersLoading) {
                _markersLoading = true
                Grpc.getInstance().tusaMarkersStub.showTusaMarkers(showTusaMarkersRequest, object : StreamObserver<ShowTusaMarkersReply> {
                    override fun onNext(value: ShowTusaMarkersReply?) {
                        val itemsList = value?.itemsList ?: return
                        println("load markers ${itemsList.size}")
                        loadedTusaMarkers = itemsList.toMutableList()
                        updateMarkers()
                    }

                    override fun onError(t: Throwable?) {
                        println("Error in MapWrapper | showTusaMarkers")
                        t?.printStackTrace()
                    }

                    override fun onCompleted() {
                        _markersLoading = false
                    }
                })
            }

        }
    }

    private fun initLocationComponent(enabled: Boolean) {
        val locationPlugin = _mapView!!.location
        locationPlugin.updateSettings {
            this.enabled = enabled
        }
        locationPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }
}