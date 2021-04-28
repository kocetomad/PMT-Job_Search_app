package com.punchy.pmt.vacansee.searchJobs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.punchy.pmt.vacansee.R
import com.punchy.pmt.vacansee.searchJobs.httpRequests.getJobDetails
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [JobDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JobDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters

    /*fun getJobDetails(jobID: Int, employerID: Int, employerName: String): MutableList<Job> {
        // TODO - Add backend request here
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val detailedJobsView = inflater.inflate(R.layout.fragment_job_details, container, false)

        // main info
        val jobTitle = detailedJobsView.findViewById<TextView>(R.id.jobTitle)
        val jobEmployer = detailedJobsView.findViewById<TextView>(R.id.employerName)
        val jobDescription = detailedJobsView.findViewById<TextView>(R.id.jobDescription)

        val jobMinSalary = detailedJobsView.findViewById<TextView>(R.id.jobMinimumSalary)
        val jobMaxSalary = detailedJobsView.findViewById<TextView>(R.id.jobMaximumSalary)

        try {
            getJobDetails(arguments?.getString("employerName")!!, arguments?.getInt("employerId")!!, arguments?.getInt("jobId")!!)

        } catch (e: Exception) {
            Log.e("JobDetailsFragment", e.toString())
        }

        // review data stuff
        val reviewScoreText = detailedJobsView.findViewById<TextView>(R.id.reviewScore)


//        val jobId = arguments?.getString("jobId")
//        val employerId = arguments?.getString("employerId")
//        val employerName = arguments?.getString("employerName")


        jobTitle.text = arguments?.getString("jobTitle")
        jobEmployer.text = arguments?.getString("employerName")
        jobDescription.text = arguments?.getString("jobDescription")

        jobMinSalary.text = "Minimum expected: ${arguments?.getFloat("minSalary")}"
        jobMaxSalary.text = "Maximum expected: ${arguments?.getFloat("maxSalary")}"

        // TODO - Bind that data to the view content (ex jobTitle.text = "stuff")

        return detailedJobsView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JobDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JobDetailsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}