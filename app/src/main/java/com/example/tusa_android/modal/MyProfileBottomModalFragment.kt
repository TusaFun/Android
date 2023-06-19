package com.example.tusa_android.modal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tusa_android.R
import com.example.tusa_android.file.FileFromStorageUtils
import com.example.tusa_android.file.ImageFileCompress
import com.example.tusa_android.image.TusaImageView
import com.example.tusa_android.my_profile.MyProfile
import com.example.tusa_android.network.file.UploadImageAndSaveToMemoryCache
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyProfileBottomModalFragment : BottomSheetDialogFragment() {

    private lateinit var _tusaImageView: TusaImageView

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if(it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                if (data != null) {
                    val uri = data.data!!
                    val handler = Handler(requireActivity().mainLooper)

                    CoroutineScope(Dispatchers.IO).launch {
                        val fileStorageUtils = FileFromStorageUtils(requireContext(), uri)
                        val tempFile = fileStorageUtils.getFilePathFromUri()
                        val imageCompressor = ImageFileCompress(tempFile, requireContext())

                        val resultCompressedImage = imageCompressor.compress(50)
                        handler.post {
                            _tusaImageView.setUri(resultCompressedImage)
                        }

                        val uploadImageAndSave = UploadImageAndSaveToMemoryCache(
                            resultCompressedImage, requireContext(), "AVATAR")
                        val path = uploadImageAndSave.start()
                    }
                }
            }
        }
    )

    // views
    private lateinit var _userAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_profile_bottom_modal, container, false)

        _tusaImageView = view.findViewById<TusaImageView>(R.id.avatarView)
        _tusaImageView.setupImageUseTryUseMemoryCache(MyProfile.instance().getAvatarPath())
        _tusaImageView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            activityResultLauncher.launch(intent)
        }

        return view
    }

    companion object {
        const val TAG = "MyProfileBottomModalFragment"
    }
}