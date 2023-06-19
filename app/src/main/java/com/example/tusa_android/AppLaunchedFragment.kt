package com.example.tusa_android

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.tusa_android.network.Authentication

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppLaunchedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppLaunchedFragment : Fragment() {
    // TODO: Rename and change types of parameters
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
        val view = inflater.inflate(R.layout.fragment_app_launched, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()

        if(Authentication.getInstance().firstStartCheck() == 1) {
            requireView().findNavController().navigate(R.id.action_appLaunchedFragment_to_mainFragment)
        } else {
            requireView().findNavController().navigate(R.id.action_appLaunchedFragment_to_loginFragment)
        }

    }

    override fun onAttach(context: Context) {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            val granted = it
        }
        if(ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
        super.onAttach(context)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppLaunchedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppLaunchedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}