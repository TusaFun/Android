package com.example.tusa_android.network.file

import com.example.tusa_android.network.AppUrl
import com.example.tusa_android.network.Authentication
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class UploadImage(
    private val bytes: ByteArray,
    private val tusaFileType: String,
    private val imageType: String = "image/jpeg",
) {

    private var _uploadedFileId : String? = null

    fun start() : String{
        try {
            val client = OkHttpClient().newBuilder().build()
            val mediaType: MediaType? = imageType.toMediaTypeOrNull()
            val body = bytes.toRequestBody(mediaType, 0, bytes.size)
            val request = Request.Builder()
                .url("${AppUrl.filesUrl}api/files/upload")
                .method("POST", body)
                .addHeader("Content-Type", imageType)
                .addHeader("Tusa-File-Type", tusaFileType)
                .addHeader("Authorization", "Bearer " + Authentication.getInstance().accessToken)
                .build()
            val response = client.newCall(request).execute()
            _uploadedFileId = response.body!!.string()
            return _uploadedFileId!!
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return ""
    }
}



