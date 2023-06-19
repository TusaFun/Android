package com.example.tusa_android.profile

class AnyProfile {
    companion object {
        fun makePathToAvatar(username: String): String {
            return "${username}/avatar.jpeg"
        }
    }


}