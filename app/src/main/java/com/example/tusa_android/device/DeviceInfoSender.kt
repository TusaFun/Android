package com.example.tusa_android.device

import com.example.tusa_android.SendTusaBatteryInfoRequest
import com.example.tusa_android.UpdateTusaUserInfoRequest
import com.example.tusa_android.network.Grpc
import com.mapbox.geojson.Point
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class DeviceInfoSender {

    val currentLatitude : Double? get() = _latitude
    val currentLongitude : Double? get() = _longitude

    val batteryLevel: Int get() = _batteryLevel
    val batteryInSaveMode: Boolean get() = _batteryInSaveMode
    val batteryState: String get() = _batteryState

    private var _latitude: Double? = null
    private var _longitude: Double? = null
    private var _scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)

    private var _batteryLevel: Int = -1
    private var _batteryInSaveMode: Boolean = false
    private var _batteryState: String = "NONE"

    private val _sendingPeriod: Long = 5
    private val _timeUnitPeriod : TimeUnit = TimeUnit.SECONDS

    private var _scheduledFuture: ScheduledFuture<*>? = null

    fun updateMyLocation(latitude: Double, longitude: Double) {
        _latitude = latitude
        _longitude = longitude
    }

    fun updateBatteryInfo(level: Int, state: String, inSaveMode: Boolean) {
        _batteryLevel = level
        _batteryState = state
        _batteryInSaveMode = inSaveMode
    }

    fun shutdown() {
        _scheduledThreadPoolExecutor.shutdown()
        _scheduledFuture?.cancel(false)
    }

    fun runSendInfo() {
        _scheduledFuture = _scheduledThreadPoolExecutor.scheduleAtFixedRate(SendDeviceInfoRunnable(this), _sendingPeriod, _sendingPeriod, _timeUnitPeriod)
    }

    class SendDeviceInfoRunnable(private val deviceInfoSender: DeviceInfoSender) : Runnable {
        override fun run() {
            try {
                val latitude = deviceInfoSender.currentLatitude
                val longitude = deviceInfoSender.currentLongitude

                if(latitude == null || longitude == null) {
                    return
                }

                val batteryRequest = SendTusaBatteryInfoRequest.newBuilder()
                    .setBatteryLevel(deviceInfoSender.batteryLevel)
                    .setState(deviceInfoSender.batteryState)
                    .setIsInSaveMode(deviceInfoSender.batteryInSaveMode)

                val request = UpdateTusaUserInfoRequest.newBuilder()
                    .setLatitude(latitude)
                    .setLongitude(longitude)
                    .setBatteryInfo(batteryRequest)
                    .build()
                val reply = Grpc.getInstance().tusaDeviceInfoStub.updateTusaUserInfo(request)
            }
            catch (exception : Exception) {
                println(exception.message)
            }
        }
    }
}