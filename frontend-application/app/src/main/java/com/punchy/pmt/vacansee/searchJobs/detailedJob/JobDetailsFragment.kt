//package com.punchy.pmt.vacansee.searchJobs.detailedJob
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.RecyclerView
//import com.punchy.pmt.vacansee.R
//import com.punchy.pmt.vacansee.searchJobs.httpRequests.DetailedJob
//import com.punchy.pmt.vacansee.searchJobs.httpRequests.Job
//import com.punchy.pmt.vacansee.searchJobs.httpRequests.getJobDetails
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.launch
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//
//var fullJob = DetailedJob(Job(), mutableListOf(), mutableListOf())
///**
// * A simple [Fragment] subclass.
// * Use the [JobDetailsFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class JobDetailsFragment : Fragment() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val detailedJobsView = inflater.inflate(R.layout.fragment_job_details, container, false)
//
//        // main info
//        val jobTitle = detailedJobsView.findViewById<TextView>(R.id.jobTitle)
//        val jobEmployer = detailedJobsView.findViewById<TextView>(R.id.employerName)
//        val jobDescription = detailedJobsView.findViewById<TextView>(R.id.jobDescription)
//
//        val jobMinSalary = detailedJobsView.findViewById<TextView>(R.id.jobMinimumSalary)
//        val jobMaxSalary = detailedJobsView.findViewById<TextView>(R.id.jobMaximumSalary)
//
//        // Inflate the layout for this fragment
//        val reviewsRecyclerView = detailedJobsView.findViewById<RecyclerView>(R.id.detailedJobReviewsRecyclerView)
//
//
//        fun loadData() = CoroutineScope(Dispatchers.Main).launch {
//
//            val task = async(Dispatchers.IO) {
//                getJobDetails(arguments?.getString("employerName")!!, arguments?.getInt("employerId")!!, arguments?.getInt("jobId")!!)
//            }
//            fullJob = task.await()
//
//            val rvAdapter = ReviewsRvAdapter(fullJob.reviewData)
//            reviewsRecyclerView.adapter = rvAdapter
//            rvAdapter.notifyDataSetChanged()
//
//            val reviewScoreText = detailedJobsView.findViewById<TextView>(R.id.reviewScore)
//
//
//            if (fullJob.reviewData.isEmpty()) {
//                // get average of all reviews
//                var reviewScoreAverage = 0.0f
//                val reviewCount = fullJob.reviewData.size
//
//                for (review in fullJob.reviewData)
//                    reviewScoreAverage += review.rating
//
//                reviewScoreAverage /= reviewCount
//                reviewScoreText.text = "${reviewScoreAverage} out of 5.0"
//            } else {
//                reviewScoreText.text = "No reviews."
//            }
//
//        }
//        loadData()
//
//        // review data stuff
//        val reviewScoreText = detailedJobsView.findViewById<TextView>(R.id.reviewScore)
//
//
////        val jobId = arguments?.getString("jobId")
////        val employerId = arguments?.getString("employerId")
////        val employerName = arguments?.getString("employerName")
//
//
//        jobTitle.text = arguments?.getString("jobTitle")
//        jobEmployer.text = arguments?.getString("employerName")
//        jobDescription.text = arguments?.getString("jobDescription")
//
//        jobMinSalary.text = "Minimum expected: £${arguments?.getFloat("minSalary")}"
//        jobMaxSalary.text = "Maximum expected: £${arguments?.getFloat("maxSalary")}"
//
//
//        val rvAdapter = ReviewsRvAdapter(reviewsList)
//        reviewsRecyclerView.adapter = rvAdapter
//
//        return detailedJobsView
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment JobDetailsFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            JobDetailsFragment().apply {
//                arguments = Bundle().apply {
//                }
//            }
//    }
//}