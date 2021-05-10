package com.punchy.pmt.vacansee

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.punchy.pmt.vacansee.searchJobs.httpRequests.login
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception

//import com.punchy.pmt.vacansee.searchJobs.httpRequests.login

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var sessionCookie = ""
var userID = ""
/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
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
    ): View {
        val loginView: View = inflater.inflate(R.layout.fragment_login, container, false)

        loginView.findViewById<Button>(R.id.loginButton)?.setOnClickListener {
            val loginEmail = loginView.findViewById<EditText>(R.id.loginEmail).text.toString()
            val loginPassword = loginView.findViewById<EditText>(R.id.loginPassword).text.toString()

            var loginResponse = arrayOf<String?>()
            fun asyncLogin(email: String, password: String) =
                CoroutineScope(Dispatchers.Main).launch {
                    val task = async(Dispatchers.IO) {
                        login(email, password)
                    }

                    loginResponse=task.await()
                    if(loginResponse[0].equals("true")){
                        sessionCookie = loginResponse[1].toString()
                        userID = loginResponse[2].toString()
                        Log.d("login", "cookie post login:"+ sessionCookie)
                        findNavController().navigate(R.id.action_loginFragment_to_jobsFragment)
                    }else{
                        Toast.makeText(context, "Bad credentials", Toast.LENGTH_SHORT).show()
                    }


                }

            try {
                asyncLogin(loginEmail, loginPassword)
            }catch (e: Exception){
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }

        }

        loginView.findViewById<Button>(R.id.registerButton).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerAccountFragment)
        }

        // Inflate the layout for this fragment
        return loginView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
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