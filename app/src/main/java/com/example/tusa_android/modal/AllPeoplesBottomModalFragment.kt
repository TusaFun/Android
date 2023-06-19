package com.example.tusa_android.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.example.tusa_android.R
import com.example.tusa_android.ScanQrCodeRequest
import com.example.tusa_android.network.Grpc
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class AllPeoplesBottomModalFragment : BottomSheetDialogFragment() {

    private var _readQrCodeActivityResult = registerForActivityResult(ScanContract()) {
        if(it.contents == null) {
            println("Read code cancelled")
        } else {
            val readCode = it.contents
            println("Code = $readCode")

            lifecycleScope.launch {
                val request = ScanQrCodeRequest.newBuilder()
                    .setCode(readCode)
                    .build()
                try {
                    val response = Grpc.getInstance().friendsQrCodes.scanQrCode(request)
                } catch (exception: Exception) {
                    println(exception.message)
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_all_peoples_bottom_modal, container, false)

        val scanQrCodeButton = view.findViewById<Button>(R.id.scanQrCodeFriend)
        scanQrCodeButton.setOnClickListener {
            val scanOptions = ScanOptions()
            scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            scanOptions.setOrientationLocked(false)
            scanOptions.setPrompt("Reading friend qr code")
            scanOptions.setBeepEnabled(false)
            _readQrCodeActivityResult.launch(scanOptions)
        }

        val createScanCodeForFriend = view.findViewById<Button>(R.id.createQrCodeForFriendButton)
        createScanCodeForFriend.setOnClickListener {
            val modal = ShowFriendQrCodeBottomModalFragment()
            modal.show(requireActivity().supportFragmentManager, ShowFriendQrCodeBottomModalFragment.TAG)
        }

        return view
    }

    companion object {
        const val TAG = "AllPeoplesBottomModalFragment"
    }
}