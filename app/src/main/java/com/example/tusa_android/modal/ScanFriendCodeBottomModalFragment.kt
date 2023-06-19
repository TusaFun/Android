package com.example.tusa_android.modal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tusa_android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScanFriendCodeBottomModalFragment : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_scan_friend_code_bottom_modal, container, false)

        return view
    }

    companion object {
        const val TAG = "ScanFriendCodeBottomModalFragment"
    }
}