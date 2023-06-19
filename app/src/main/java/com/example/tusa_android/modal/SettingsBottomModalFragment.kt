package com.example.tusa_android.modal

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.tusa_android.R
import com.example.tusa_android.network.Authentication
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsBottomModalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsBottomModalFragment : BottomSheetDialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_settings_bottom_modal, container, false)

        val signOutButton = view.findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            Authentication.getInstance().signOut()
            findNavController().navigate(R.id.loginFragment)
            dismiss()
        }


        return view
    }

    companion object {
        const val TAG = "SettingsBottomModalFragment"
    }
}