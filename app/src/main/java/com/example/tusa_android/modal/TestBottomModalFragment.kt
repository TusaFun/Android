package com.example.tusa_android.modal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tusa_android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class TestBottomModalFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_bottom_modal, container, false)
    }

    companion object {
        const val TAG = "TestModalBottomSheet"
    }
}