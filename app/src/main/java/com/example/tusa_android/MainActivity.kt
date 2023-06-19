package com.example.tusa_android

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tusa_android.cache.MemoryBitmapsCache
import com.example.tusa_android.my_profile.MyProfile
import com.example.tusa_android.network.AppUrl
import com.example.tusa_android.network.Authentication
import com.example.tusa_android.network.Grpc
import com.example.tusa_android.screen.ScreenMetricsUtils


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Grpc.createInstance(AppUrl.grpcHost, AppUrl.grpcPort)

        val sharedPreferences = getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
        Authentication.createInstance(sharedPreferences)

        MemoryBitmapsCache.createInstance(savedInstanceState)

        MyProfile.createInstance()

    }
}