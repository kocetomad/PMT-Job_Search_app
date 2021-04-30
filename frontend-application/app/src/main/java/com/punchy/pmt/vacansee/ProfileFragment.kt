package com.punchy.pmt.vacansee

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.punchy.pmt.vacansee.searchJobs.httpRequests.getSavedJobs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // remove action bar shadow
        (activity as AppCompatActivity?)!!.supportActionBar?.elevation = 0f
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
        // Inflate the layout for this fragment
        val profileView = inflater.inflate(R.layout.fragment_profile, container, false)

        profileView.findViewById<Button>(R.id.goToJobsSearch).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_jobsFragment)
        }

        val bottomSheetView = profileView.findViewById<LinearLayout>(R.id.savedJobsBackdrop)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        //val savedJobsList = getSavedJobs()

//        if (savedJobsList.isEmpty()) {
//            // get progress bar and hide it after the jobs load.
//            bottomSheetView.findViewById<ProgressBar>(R.id.savedJobsProgressBar).visibility = View.GONE
//
//            // get error view and make it visible if the fetching fails
//            bottomSheetView.findViewById<TextView>(R.id.savedJobsErrorText).text =
//                "No saved jobs found"
//            bottomSheetView.findViewById<LinearLayout>(R.id.savedJobsErrorView).visibility = View.VISIBLE
//        } else {
//            // get progress bar and hide it after the jobs load.
//            bottomSheetView.findViewById<ProgressBar>(R.id.savedJobsProgressBar).visibility = View.GONE
//        }

        return profileView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}