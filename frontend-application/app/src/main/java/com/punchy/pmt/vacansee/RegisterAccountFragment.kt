package com.punchy.pmt.vacansee

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import okhttp3.*
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterAccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val client = OkHttpClient()
    private fun postAccountData(username: String, interestField: String, email: String, password: String, passwordConfirm: String) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("interestField", interestField)
            .add("email", email)
            .add("password", password)
            .add("passwordConfirm", passwordConfirm)
            .build()

        val request = Request.Builder()
            .url("https://reqbin.com/echo/post/json")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })
    }

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
        val registerAccountView = inflater.inflate(R.layout.fragment_register_account, container, false)

        val createAccountButton = registerAccountView.findViewById<Button>(R.id.createAccountButton)
        createAccountButton.setOnClickListener {
            val username = registerAccountView.findViewById<EditText>(R.id.registerUsername)
            val fieldOfInterest = registerAccountView.findViewById<EditText>(R.id.registerInterest)
            val email = registerAccountView.findViewById<EditText>(R.id.registerEmail)
            val password = registerAccountView.findViewById<EditText>(R.id.registerPassword)
            val confirmPassword = registerAccountView.findViewById<EditText>(R.id.confirmRegisterPassword)

            if (username.text.isEmpty()) {
                username.error = "Username required"
            } else if (fieldOfInterest.text.isEmpty()) {
                fieldOfInterest.error = "Field of interest required"
            } else if (email.text.isEmpty()) {
                email.error = "Email required"
            } else if(password.text.isEmpty() && confirmPassword.text.isEmpty()) {
                password.error = "Password required"
            } else if (password.text.isNotEmpty() && confirmPassword.text.isEmpty()) {
                confirmPassword.error = "Please confirm your password"
            } else if (password.text.isNotEmpty() && confirmPassword.text.isNotEmpty() && password.text.toString() != confirmPassword.text.toString()) {
                confirmPassword.error = "Passwords don't match"
            } else {
                username.error = null
                fieldOfInterest.error = null
                email.error = null
                password.error = null
                confirmPassword.error = null

                postAccountData(username.text.toString(), fieldOfInterest.text.toString(), email.text.toString(), password.text.toString(), confirmPassword.text.toString())
                // TODO - Let the user know the account has been created successfully. Send him to login screen
            }
        }
        // Inflate the layout for this fragment
        return registerAccountView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterAccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}