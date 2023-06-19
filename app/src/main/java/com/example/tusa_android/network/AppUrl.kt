package com.example.tusa_android.network

class AppUrl {
    companion object {
        val grpcHost: String get() = "192.168.2.3"
        val grpcPort: Int get() = 5010
        val requestsUrl: String get() = "http://192.168.0.34:5010/"
        val filesUrl: String get() = "http://37.140.197.204:5011/"
    }
}