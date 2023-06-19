package com.example.tusa_android.my_profile

import com.example.tusa_android.TusaMeReply
import com.example.tusa_android.TusaMeRequest
import com.example.tusa_android.UpdateTusaPropertyRequest
import com.example.tusa_android.network.Authentication
import com.example.tusa_android.network.Grpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyProfile {
    companion object {
        fun instance() : MyProfile = instance

        fun createInstance() {
            instance = MyProfile()
        }

        private lateinit var instance: MyProfile
    }

    val username: String get() = tusaMeReply!!.username

    val meReply: TusaMeReply? get() = tusaMeReply

    private var tusaMeReply: TusaMeReply? = null

    fun loadMyProfile() {
        val request = TusaMeRequest.newBuilder().build()
        tusaMeReply = Grpc.getInstance().tusaUserStub.tusaMe(request)
    }

    fun getAvatarPath() : String {
        return "${tusaMeReply!!.username}/avatar.jpeg"
    }

}