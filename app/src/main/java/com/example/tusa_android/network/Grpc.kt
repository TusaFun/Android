package com.example.tusa_android.network

import com.example.tusa_android.*
import com.example.tusa_android.FriendsQrCodesGrpc.FriendsQrCodesBlockingStub
import com.example.tusa_android.TusaChatsGrpc.TusaChatsBlockingStub
import com.example.tusa_android.TusaChatsGrpc.TusaChatsStub
import com.example.tusa_android.TusaCommunicationsGrpc.TusaCommunicationsBlockingStub
import com.example.tusa_android.TusaDeviceInfoGrpc.TusaDeviceInfoBlockingStub
import com.example.tusa_android.TusaDeviceInfoGrpc.TusaDeviceInfoStub
import com.example.tusa_android.TusaFileRequesterGrpc.TusaFileRequesterBlockingStub
import com.example.tusa_android.TusaMarkersGrpc.TusaMarkersBlockingStub
import com.example.tusa_android.TusaMarkersGrpc.TusaMarkersStub
import com.example.tusa_android.TusaUploaderGrpc.TusaUploaderBlockingStub
import com.example.tusa_android.TusaUploaderGrpc.TusaUploaderFutureStub
import com.example.tusa_android.TusaUploaderGrpc.TusaUploaderStub
import com.example.tusa_android.TusaUserGrpc.TusaUserBlockingStub
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.MetadataUtils

class Grpc {
    companion object {
        fun getInstance() : Grpc = _instance!!

        fun createInstance(host: String, port: Int) {
            assert(_instance == null) {
                "Grpc instance created already"
            }
            val instance = Grpc()
            _instance = instance
            instance.createChannel(host, port)
            instance.createStubs(null)
        }

        private var _instance: Grpc? = null
    }

    val tusaUserStub: TusaUserBlockingStub get() = _tusaUserStub!!
    val tusaDeviceInfoStub: TusaDeviceInfoBlockingStub get() = _tusaDeviceInfoStub!!
    val tusaMarkersStub: TusaMarkersStub get() = _tusaMarkersStub!!
    val friendsQrCodes: FriendsQrCodesBlockingStub get() = _friendsQrCodesStub!!
    val tusaChatsStub: TusaChatsBlockingStub get() = _tusaChatsStub!!
    val tusaCommunicationsBlockingStub: TusaCommunicationsBlockingStub get() = _tusaCommunicationsStub!!

    private var _channel: ManagedChannel? = null
    private var _tusaUserStub: TusaUserBlockingStub? = null
    private var _tusaDeviceInfoStub: TusaDeviceInfoBlockingStub? = null
    private var _tusaMarkersStub: TusaMarkersStub? = null
    private var _friendsQrCodesStub: FriendsQrCodesBlockingStub? = null
    private var _tusaChatsStub: TusaChatsBlockingStub? = null
    private var _tusaCommunicationsStub: TusaCommunicationsBlockingStub? = null

    fun updateAccessToken(accessToken: String) {
        createStubs(accessToken)
    }

    private fun createChannel(host: String, port: Int) {
        _channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
    }

    private fun createStubs(accessToken: String?) {
        val headerMetadata = io.grpc.Metadata()
        val key: io.grpc.Metadata.Key<String> = io.grpc.Metadata.Key.of("Authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
        if(!accessToken.isNullOrEmpty()) {
            headerMetadata.put(key, "Bearer $accessToken")
        }

        val interceptor = MetadataUtils.newAttachHeadersInterceptor(headerMetadata)
        _tusaUserStub = TusaUserGrpc.newBlockingStub(_channel).withInterceptors(interceptor)
        _tusaDeviceInfoStub = TusaDeviceInfoGrpc.newBlockingStub(_channel).withInterceptors(interceptor)
        _tusaMarkersStub = TusaMarkersGrpc.newStub(_channel).withInterceptors(interceptor)
        _friendsQrCodesStub = FriendsQrCodesGrpc.newBlockingStub(_channel).withInterceptors(interceptor)
        _tusaChatsStub = TusaChatsGrpc.newBlockingStub(_channel).withInterceptors(interceptor)
        _tusaCommunicationsStub = TusaCommunicationsGrpc.newBlockingStub(_channel).withInterceptors(interceptor)
    }

}