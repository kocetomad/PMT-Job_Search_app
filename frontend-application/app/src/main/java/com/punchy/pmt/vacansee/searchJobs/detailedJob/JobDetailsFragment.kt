package com.punchy.pmt.vacansee.searchJobs.detailedJob

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.punchy.pmt.vacansee.R
import com.punchy.pmt.vacansee.searchJobs.httpRequests.DetailedJob
import com.punchy.pmt.vacansee.searchJobs.httpRequests.FinanceData
import com.punchy.pmt.vacansee.searchJobs.httpRequests.Job
import com.punchy.pmt.vacansee.searchJobs.httpRequests.getJobDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

var fullJob = DetailedJob(Job(), mutableListOf(), mutableListOf())
var financeData = listOf<FinanceData>()
/**
 * A simple [Fragment] subclass.
 * Use the [JobDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JobDetailsFragment : Fragment() {
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

        val jobSalaryText = detailedJobsView.findViewById<TextView>(R.id.salaryText)
        val jobMinSalary = detailedJobsView.findViewById<TextView>(R.id.jobMinimumSalary)
        val jobMaxSalary = detailedJobsView.findViewById<TextView>(R.id.jobMaximumSalary)

        val jobReviewStar1 = detailedJobsView.findViewById<ImageView>(R.id.star1)
        val jobReviewStar2 = detailedJobsView.findViewById<ImageView>(R.id.star2)
        val jobReviewStar3 = detailedJobsView.findViewById<ImageView>(R.id.star3)
        val jobReviewStar4 = detailedJobsView.findViewById<ImageView>(R.id.star4)
        val jobReviewStar5 = detailedJobsView.findViewById<ImageView>(R.id.star5)

        // Inflate the layout for this fragment
        val reviewsRecyclerView =
            detailedJobsView.findViewById<RecyclerView>(R.id.detailedJobReviewsRecyclerView)


        fun loadData() = CoroutineScope(Dispatchers.Main).launch {

            val task = async(Dispatchers.IO) {
                getJobDetails(
                    arguments?.getString("employerName")!!,
                    arguments?.getInt("employerId")!!,
                    arguments?.getInt("jobId")!!
                )
            }
            fullJob = task.await()
            val newFragment: Fragment = FinanceGraph()
            val fragmentTransaction = childFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.newGraphLayoutView, newFragment)
            fragmentTransaction.commit()

            financeData = fullJob.financeData

            jobTitle.text = fullJob.jobDetails.jobTitle
            jobEmployer.text = fullJob.jobDetails.employerName
            jobDescription.text =
                Html.fromHtml(fullJob.jobDetails.jobDescription, Html.FROM_HTML_MODE_COMPACT)

            jobSalaryText.text = "Salary - ${fullJob.jobDetails.salaryType}"

            jobMinSalary.text = "Minimum expected: £${fullJob.jobDetails.minimumSalary}"
            jobMaxSalary.text = "Maximum expected: £${fullJob.jobDetails.maximumSalary}"


            val rvAdapter = ReviewsRvAdapter(fullJob.reviewData)
            reviewsRecyclerView.adapter = rvAdapter
            rvAdapter.notifyDataSetChanged()

            val reviewScoreText = detailedJobsView.findViewById<TextView>(R.id.reviewScore)

            if (fullJob.reviewData.isNotEmpty()) {
                // get average of all reviews
                var reviewScoreAverage = 0.0f
                val reviewCount = fullJob.reviewData.size

                for (review in fullJob.reviewData)
                    reviewScoreAverage += review.rating

                reviewScoreAverage /= reviewCount
                reviewScoreText.text = "${reviewScoreAverage} out of 5.0"

                // set the stars
                when {
                    reviewScoreAverage > 4.5f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar4.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar5.setImageResource(R.drawable.ic_baseline_star_24)
                    }
                    reviewScoreAverage > 4.0f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar4.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar5.setImageResource(R.drawable.ic_baseline_star_half_24)
                    }
                    reviewScoreAverage > 3.5f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar4.setImageResource(R.drawable.ic_baseline_star_24)
                    }
                    reviewScoreAverage > 3.0f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar4.setImageResource(R.drawable.ic_baseline_star_half_24)
                    }
                    reviewScoreAverage > 2.5f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                    }
                    reviewScoreAverage > 2.0f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar3.setImageResource(R.drawable.ic_baseline_star_half_24)
                    }
                    reviewScoreAverage > 1.5f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                    }
                    reviewScoreAverage > 1.0f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                        jobReviewStar2.setImageResource(R.drawable.ic_baseline_star_half_24)
                    }
                    reviewScoreAverage > 0.5f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                    }
                    reviewScoreAverage > 0.0f -> {
                        jobReviewStar1.setImageResource(R.drawable.ic_baseline_star_half_24)
                    }
                }
            } else {
                reviewScoreText.text = "No reviews."
            }

        }
        loadData()

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