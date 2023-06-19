package com.example.tusa_android.main_fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tusa_android.R
import com.example.tusa_android.device.DeviceInfoSender
import com.example.tusa_android.map.MapWrapper
import com.example.tusa_android.map.markers.DebugMarkersDrawView
import com.example.tusa_android.modal.MyProfileBottomModalFragment
import com.example.tusa_android.modal.SettingsBottomModalFragment
import com.example.tusa_android.modal.TestBottomModalFragment
import com.example.tusa_android.my_profile.MyProfile
import com.example.tusa_android.network.Authentication
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.Plugin
import kotlinx.coroutines.launch


class MainFragment : Fragment() {

    private lateinit var _deviceInfoSender: DeviceInfoSender
    private lateinit var _mapView: MapView
    private lateinit var _mapWrapper: MapWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Отправляем информацию о устройстве в цикле
        _deviceInfoSender = DeviceInfoSender()
        _deviceInfoSender.runSendInfo()

        _mapWrapper = MapWrapper(_deviceInfoSender, requireContext(), lifecycleScope)

        lifecycleScope.launch {
            try {
                MyProfile.instance().loadMyProfile()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val debugMapDrawView = view.findViewById<DebugMarkersDrawView>(R.id.debugMarkersDraw)

        // Map view
        _mapView = view.findViewById<MapView>(R.id.mapView)
        _mapWrapper.readyMapView(_mapView, debugMapDrawView)

        val mainFragmentButtonsListener = MainFragmentButtonsListener(
            view,
            requireActivity(),
            _mapView,
            _deviceInfoSender
        )
        mainFragmentButtonsListener.listen()



        return view
    }

    @SuppressLint("Lifecycle")
    override fun onStart() {
        super.onStart()
        _mapWrapper.onStart()
    }

    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        _mapWrapper.onStop()
        _deviceInfoSender.shutdown()
    }

    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        _mapWrapper.onLowMemory()
    }

    @SuppressLint("Lifecycle")
    override fun onDestroy() {
        super.onDestroy()
        _mapWrapper.onDestroy()
    }
}