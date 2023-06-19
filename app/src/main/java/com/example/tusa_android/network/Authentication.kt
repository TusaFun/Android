package com.example.tusa_android.network

import android.content.SharedPreferences
import androidx.navigation.findNavController
import com.example.tusa_android.R
import com.example.tusa_android.RefreshTokenRequest
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Authentication {
    companion object {
        fun getInstance(): Authentication {
            return _instance!!
        }

        fun createInstance(sharedPreferences: SharedPreferences) {
            _instance = Authentication()
            _instance!!._sharedPreferences = sharedPreferences
        }

        private var _instance: Authentication? = null
    }

    val accessToken: String get() = _accessToken
    val refreshToken: String get() = _refreshToken

    private var _accessToken: String = ""
    private var _refreshToken: String = ""
    private lateinit var _sharedPreferences: SharedPreferences
    private val scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)
    private var _scheduledFuture: ScheduledFuture<*>? = null

    fun signOut() {
        _sharedPreferences.edit().clear().apply()
        _scheduledFuture?.cancel(false)
    }

    fun firstStartCheck(): Int {
        val accessToken = _sharedPreferences.getString("ACCESS_TOKEN", null)
        val refreshToken = _sharedPreferences.getString("REFRESH_TOKEN", null)
        if(!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            _accessToken = accessToken
            _refreshToken = refreshToken
            UpdateAuthenticationInformation().run()
            return 1
        }
        return 0
    }

    fun login(accessToken: String, refreshToken: String, period: Long) {
        Grpc.getInstance().updateAccessToken(accessToken)
        _sharedPreferences
            .edit()
            .putString("ACCESS_TOKEN", accessToken)
            .putString("REFRESH_TOKEN", refreshToken)
            .apply()
        _accessToken = accessToken
        _refreshToken = refreshToken
        runTokenRefresh(period)
    }

    private fun runTokenRefresh(period: Long) {
        _scheduledFuture = scheduledThreadPoolExecutor.schedule(UpdateAuthenticationInformation(), period, TimeUnit.SECONDS)
    }

    private fun newAuthenticationStep(accessToken: String, refreshToken: String, period: Long) {
        Grpc.getInstance().updateAccessToken(accessToken)

        _refreshToken = refreshToken
        _accessToken = accessToken
        _sharedPreferences
            .edit()
            .clear()
            .putString("ACCESS_TOKEN", accessToken)
            .putString("REFRESH_TOKEN", refreshToken)
            .apply()

        runTokenRefresh(period)
    }

    class UpdateAuthenticationInformation : Runnable{
        override fun run() {
            try {
                val request = RefreshTokenRequest.newBuilder()
                    .setAccessToken(getInstance().accessToken)
                    .setRefreshToken(getInstance().refreshToken)
                    .build()
                val reply = Grpc.getInstance().tusaUserStub.refreshToken(request)
                val period = 100L
                getInstance().newAuthenticationStep(reply.accessToken, reply.refreshToken, period)
                println("Authentication tokens refreshed")
            } catch (exception: Exception) {
                getInstance().newAuthenticationStep(getInstance().accessToken, getInstance().refreshToken, 10L)
            }
        }
    }
}