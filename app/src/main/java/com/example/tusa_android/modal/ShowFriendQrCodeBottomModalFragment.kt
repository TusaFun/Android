package com.example.tusa_android.modal

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.example.tusa_android.CreateQrCodeRequest
import com.example.tusa_android.R
import com.example.tusa_android.network.Grpc
import com.example.tusa_android.qr_code.QrCodeGenerator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class ShowFriendQrCodeBottomModalFragment : BottomSheetDialogFragment() {
    private lateinit var _qrCodeImageView: ImageView
    private lateinit var _progressBar: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(
            R.layout.fragment_show_friend_qr_code_bottom_modal,
            container,
            false
        )

        _qrCodeImageView = view.findViewById(R.id.qrCodeImage)
        _progressBar = view.findViewById(R.id.progress_circular)

        lifecycleScope.launch {
            val handler = Handler(Looper.getMainLooper())
            val request = CreateQrCodeRequest.newBuilder().build()
            val response = Grpc.getInstance().friendsQrCodes.createQrCode(request)
            val code = response.code
            val bitmapQrCode = QrCodeGenerator.generateQrCode(code)
            handler.post {
                _qrCodeImageView.setImageBitmap(bitmapQrCode)
                _progressBar.hide()
            }
        }

        return view
    }

    companion object {
        const val TAG = "ShowFriendQrCodeBottomModalFragment"
    }
}