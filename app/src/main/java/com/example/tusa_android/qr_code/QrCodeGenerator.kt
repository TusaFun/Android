package com.example.tusa_android.qr_code

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder


class QrCodeGenerator {
    companion object
    {
        fun generateQrCode(contentToEncode: String): Bitmap {
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.encodeBitmap(contentToEncode, BarcodeFormat.QR_CODE, 500, 500)
        }
    }

}