package com.example.tusa_android.main_fragment

import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.example.tusa_android.R
import com.example.tusa_android.device.DeviceInfoSender
import com.example.tusa_android.modal.AllPeoplesBottomModalFragment
import com.example.tusa_android.modal.MyProfileBottomModalFragment
import com.example.tusa_android.modal.SettingsBottomModalFragment
import com.example.tusa_android.modal.TestBottomModalFragment
import com.example.tusa_android.modal.chat.PersonChatsBottomModalFragment
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView

class MainFragmentButtonsListener(
    private val view: View,
    private val fragmentActivity: FragmentActivity,
    private val mapView: MapView,
    private val deviceInfoSender: DeviceInfoSender
    ) {

    fun listen() {
        val messengerButton = view.findViewById<Button>(R.id.messengerButton)
        messengerButton.setOnClickListener {
            val modal = PersonChatsBottomModalFragment()
            modal.show(fragmentActivity.supportFragmentManager, PersonChatsBottomModalFragment.TAG)
        }

        val settingsButton = view.findViewById<Button>(R.id.mySettingsButton)
        settingsButton.setOnClickListener {
            val modal = SettingsBottomModalFragment()
            modal.show(fragmentActivity.supportFragmentManager, SettingsBottomModalFragment.TAG)
        }

        val myLocationButton = view.findViewById<Button>(R.id.myLocationButton)
        myLocationButton.setOnClickListener {
            val point = Point.fromLngLat(deviceInfoSender.currentLongitude!!, deviceInfoSender.currentLatitude!!)
            val cameraOptions = CameraOptions.Builder().center(point).build()
            mapView.getMapboxMap().setCamera(cameraOptions)
        }

        val myProfileButton = view.findViewById<Button>(R.id.personalPageButton)
        myProfileButton.setOnClickListener {
            val modal = MyProfileBottomModalFragment()
            modal.show(fragmentActivity.supportFragmentManager, MyProfileBottomModalFragment.TAG )
        }

        val addFriendButton = view.findViewById<Button>(R.id.peoplesButton)
        addFriendButton.setOnClickListener {
            val modal = AllPeoplesBottomModalFragment()
            modal.show(fragmentActivity.supportFragmentManager, AllPeoplesBottomModalFragment.TAG)
        }
    }

}