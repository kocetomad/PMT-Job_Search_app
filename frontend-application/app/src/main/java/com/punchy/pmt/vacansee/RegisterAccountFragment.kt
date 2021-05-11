package com.punchy.pmt.vacansee

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.punchy.pmt.vacansee.searchJobs.httpRequests.registerAccount
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

//import com.punchy.pmt.vacansee.searchJobs.httpRequests.registerAccount

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

    private val ageRequirement = 16

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

            if (firstName.text.isNotEmpty() && lastName.text.isNotEmpty() && username.text.isNotEmpty() && dateOfBirth.text.isNotEmpty() && email.text.isNotEmpty() && password.text.isNotEmpty() && confirmPassword.text.isNotEmpty())
                if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()){
                    email.error = "Invalid email address"
                }
                else if (password.text.toString() != confirmPassword.text.toString()) {
                    confirmPassword.error = "Passwords don't match"
                } else {
                    val dateTemplate = SimpleDateFormat("dd/MM/yyyy")
                    dateTemplate.isLenient = false
                    try {
                        val date = dateTemplate.parse(dateOfBirth.text.toString())

                        val day = DateFormat.format("dd", date)
                        val month = DateFormat.format("MM", date)
                        val year = DateFormat.format("yyyy", date)

                        val currentYear = DateFormat.format("yyyy", Calendar.getInstance().time)

                        if (Integer.parseInt(day.toString()) !in 1..31)
                            dateOfBirth.error = "Invalid day range"
                        else if (Integer.parseInt(month.toString()) !in 1..12)
                            dateOfBirth.error = "Invalid month range"
                        else if (Integer.parseInt(year.toString()) < 1920)
                            dateOfBirth.error = "Invalid year range"
                        else if(Integer.parseInt(year.toString()) >= Integer.parseInt(currentYear.toString()) - ageRequirement)
                            dateOfBirth.error = "You must be over $ageRequirement to sign up"
                        else
                            if (checkWIFI(context)) {
                                registerAccount(
                                    username.text.toString(),
                                    email.text.toString(),
                                    password.text.toString(),
                                    confirmPassword.text.toString(),
                                    firstName.text.toString(),
                                    lastName.text.toString(),
                                    "$year-$month-$day"
                                )
                            } else {
                                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: ParseException) {
                        dateOfBirth.error = "Date is invalid."
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