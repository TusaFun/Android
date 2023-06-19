package com.example.tusa_android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.example.tusa_android.network.Grpc
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class RegistrationFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        val loginTextInputLayout = view.findViewById<TextInputLayout>(R.id.loginInputLayout)
        val passwordTextInputLayout = view.findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val confirmPasswordTextInputLayout = view.findViewById<TextInputLayout>(R.id.confirmPasswordInputLayout)

        val loginEditText = view.findViewById<TextInputEditText>(R.id.loginInput)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordEditText = view.findViewById<TextInputEditText>(R.id.confirmPasswordInput)

        val formMessageText = view.findViewById<TextView>(R.id.registrationFormMessage)

        view.findViewById<Button>(R.id.iHaveAcount).setOnClickListener {
            val navController = view.findNavController()
            navController.navigate(R.id.action_registrationFragment_to_loginFragment)
        }

        view.findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            loginEditText.text!!.isEmpty().let {
                if(it) {
                    loginTextInputLayout.helperText = "Can't be empty"
                    return@setOnClickListener
                }
                loginTextInputLayout.helperText = ""
            }

            passwordEditText.text!!.isEmpty().let {
                if(it) {
                    passwordTextInputLayout.helperText = "Can't be empty"
                    return@setOnClickListener
                }
                passwordTextInputLayout.helperText = ""
            }

            confirmPasswordEditText.text!!.isEmpty().let {
                if(it) {
                    passwordTextInputLayout.helperText = "Can't be empty"
                    return@setOnClickListener
                }
                passwordTextInputLayout.helperText = ""
            }

            if(confirmPasswordEditText.text != passwordEditText.text) {
                confirmPasswordTextInputLayout.helperText = "Confirmation password wrong"
            } else {
                confirmPasswordTextInputLayout.helperText = ""
            }

            val username = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            try {
                println("Logining...")
                val createMeRequest = CreateTusaMeRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build()
                val createMeReply = Grpc.getInstance().tusaUserStub.createTusaMe(createMeRequest)

                val navController = view.findNavController()
                navController.navigate(R.id.action_registrationFragment_to_loginFragment)

            } catch (exception: Exception) {
                println(exception.message)
                formMessageText.text = exception.message
            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegistrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}