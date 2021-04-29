package com.punchy.pmt.vacansee

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.punchy.pmt.vacansee.searchJobs.httpRequests.registerAccount

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
        val registerAccountView =
            inflater.inflate(R.layout.fragment_register_account, container, false)

        val createAccountButton = registerAccountView.findViewById<Button>(R.id.createAccountButton)

        createAccountButton.setOnClickListener {
            val firstName = registerAccountView.findViewById<EditText>(R.id.registerFirstName)
            val lastName = registerAccountView.findViewById<EditText>(R.id.registerLastName)
            val username = registerAccountView.findViewById<EditText>(R.id.registerUsername)
            val dateOfBirth = registerAccountView.findViewById<EditText>(R.id.registerDob)
            val email = registerAccountView.findViewById<EditText>(R.id.registerEmail)

            val password = registerAccountView.findViewById<EditText>(R.id.registerPassword)
            val confirmPassword = registerAccountView.findViewById<EditText>(R.id.registerPassword2)

            val formView = registerAccountView.findViewById<LinearLayout>(R.id.loginForm)

            val childCount = formView.childCount
            for (index in 0 until childCount) {
                val childView = formView.getChildAt(index) as EditText
                if (childView.text.isEmpty()) {
                    childView.error = "${childView.hint} required"
                } else {
                    childView.error = null
                }
            }

            if (firstName.text.isEmpty() || lastName.text.isEmpty() || username.text.isEmpty() || dateOfBirth.text.isEmpty() || email.text.isEmpty()) {
                if (password.text.toString() != confirmPassword.text.toString()) {
                    confirmPassword.error = "Passwords don't match"
                } else {
                    confirmPassword.error = null

                    registerAccount(
                        username.text.toString(),
                        email.text.toString(),
                        password.text.toString(),
                        confirmPassword.text.toString(),
                        firstName.text.toString(),
                        lastName.text.toString()
                    )
                }
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