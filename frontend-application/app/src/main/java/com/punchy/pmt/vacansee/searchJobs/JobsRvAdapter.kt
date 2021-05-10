package com.punchy.pmt.vacansee.searchJobs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.punchy.pmt.vacansee.R
import com.punchy.pmt.vacansee.searchJobs.detailedJob.FinanceGraph
import com.punchy.pmt.vacansee.searchJobs.detailedJob.ReviewsRvAdapter
import com.punchy.pmt.vacansee.searchJobs.detailedJob.financeData
import com.punchy.pmt.vacansee.searchJobs.detailedJob.fullJob
import com.punchy.pmt.vacansee.searchJobs.httpRequests.Job
import com.punchy.pmt.vacansee.searchJobs.httpRequests.getJobDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.URL


//input parameter has to be changed to an object containing the data from the query
class JobsRvAdapter(val jobsList: MutableList<Job>, val parentFragment: Fragment) :
    RecyclerView.Adapter<JobsRvAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.quality_image)
        val jobTitle = itemView.findViewById<TextView>(R.id.entryJobTitle)
        val jobEmployerName = itemView.findViewById<TextView>(R.id.entryEmployerName)
        val jobDescription = itemView.findViewById<TextView>(R.id.entryJobDescription)
        var jobID = 0
        val jobSalaryText = itemView.findViewById<TextView>(R.id.entrySalaryText)
        val jobSalaryMin = itemView.findViewById<TextView>(R.id.entrySalaryMin)
        val jobSalaryMax = itemView.findViewById<TextView>(R.id.entrySalaryMax)
    }

    override fun onCreateViewHolder(view: ViewGroup, index: Int): ViewHolder {
        val v = LayoutInflater.from(view.context).inflate(R.layout.job_entry_layout, view, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return jobsList.size
    }

    var isSaved = false
    fun setItemSaved(state: Boolean) {
        isSaved = state
    }

    //the populates the view with the data from the query. Has to be changed to get the data from the object not just a string
    override fun onBindViewHolder(view: ViewHolder, index: Int) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        view.jobTitle?.text = jobsList[index].jobTitle
        view.jobEmployerName?.text = jobsList[index].employerName
        view.jobDescription?.text =
            Html.fromHtml(jobsList[index].jobDescription, Html.FROM_HTML_MODE_COMPACT)
        view.jobID = jobsList[index].jobId

        val url = URL(jobsList[index].logoUrl)

//        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

        fun loadImage() = CoroutineScope(Dispatchers.Main).launch {
            val task = async(Dispatchers.IO) {
                try {
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } catch (e: FileNotFoundException) {
                    Log.e("JobsRvAdapter - Icon grab", "$e for URL: ${jobsList[index].jobUrl}")
                    null
                }
            }

            val bmp = task.await()
            if (bmp != null) {
                view.image.setImageBitmap(bmp)
                }
        }
        loadImage()

        if (jobsList[index].minimumSalary == -1f && jobsList[index].maximumSalary == -1f) {
            view.jobSalaryMin?.text = "TBD"
            view.jobSalaryMax?.visibility = View.GONE
        } else {
            if (jobsList[index].salaryType.isEmpty())
                view.jobSalaryText?.text = "Salary type: UNKNWN"
            else
                view.jobSalaryText?.text = "Salary type: ${jobsList[index].salaryType}:"

            view.jobSalaryMin?.text = "Min: £${jobsList[index].minimumSalary}"
            view.jobSalaryMax?.text = "Max: £${jobsList[index].maximumSalary}"
        }

        view.itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card)
            ?.setOnClickListener {
                // create the bundle to send to the JobDetails fragment
                val args = Bundle()

                // args for getting job extra details
                args.putString("employerName", jobsList[index].employerName)
                args.putInt("employerId", jobsList[index].employerId)
                args.putInt("jobId", jobsList[index].jobId)

                try {
                    parentFragment.findNavController()
                        .navigate(R.id.action_jobsFragment_to_jobDetailsFragment, args)
                } catch (e: Exception) {
                    parentFragment.findNavController()
                        .navigate(R.id.action_profileFragment_to_jobDetailsFragment, args)
                }

            }
    }
}