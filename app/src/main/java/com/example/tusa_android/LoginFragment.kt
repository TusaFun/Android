package com.example.tusa_android

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.example.tusa_android.network.Authentication
import com.example.tusa_android.network.Grpc
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val loginTextInputLayout = view.findViewById<TextInputLayout>(R.id.loginInputLayout)
        val passwordTextInputLayout = view.findViewById<TextInputLayout>(R.id.passwordInputLayout)

        val loginEditText = view.findViewById<TextInputEditText>(R.id.loginInput)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.passwordInput)

        val formMessageText = view.findViewById<TextView>(R.id.loginFormMessage)


        view.findViewById<Button>(R.id.iDontHaveAccount).setOnClickListener {
            val navController = view.findNavController()
            navController.navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        // on click create account button
        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
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

            val username = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            try {
                println("Logining...")
                val tusaTokenRequest = TusaTokenRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build()

                val tusaTokenReply = Grpc.getInstance().tusaUserStub.tusaToken(tusaTokenRequest)
                val period = 10L
                Authentication.getInstance().login(
                    tusaTokenReply.accessToken,
                    tusaTokenReply.refreshToken,
                    period
                )

                // go to main screen
                val navController = view.findNavController()
                navController.navigate(R.id.action_loginFragment_to_mainFragment)

            } catch (exception: Exception) {
                println(exception.message)
                formMessageText.text = exception.message
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}