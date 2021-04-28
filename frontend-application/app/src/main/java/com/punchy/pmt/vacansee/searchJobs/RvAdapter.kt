package com.punchy.pmt.vacansee.searchJobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.punchy.pmt.vacansee.R

//input parameter has to be changed to an object containing the data from the query
class RvAdapter(val jobsList: MutableList<Job>, val parentFragment: Fragment) :
    RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(view: ViewGroup, index: Int): ViewHolder {
        val v =
            LayoutInflater.from(view?.context).inflate(R.layout.job_entry_layout, view, false)
        return ViewHolder(v)

    }

    override fun getItemCount(): Int {
        return jobsList.size
    }

    var isSaved = false
    fun setItemSaved(state: Boolean): Boolean {
        isSaved = state
        return isSaved
    }

    //the populates the view with the data from the query. Has to be changed to get the data from the object not just a string
    override fun onBindViewHolder(view: ViewHolder, index: Int) {
        view.jobTitle?.text = jobsList[index].jobTitle
        view.jobEmployerName?.text = jobsList[index].employerName
        view.jobDescription?.text = jobsList[index].jobDescription

        view.jobSalaryMin?.text = "${jobsList[index].currency} ${jobsList[index].minimumSalary}"
        view.jobSalaryMax?.text = "${jobsList[index].currency} ${jobsList[index].maximumSalary}"

        view.itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card)

            ?.setOnClickListener {
                println(index)

                // create the bundle to send to the JobDetails fragment
                val args = Bundle()
                args.putString("jobTitle", jobsList[index].jobTitle)
                args.putString("employerName", jobsList[index].employerName)
                args.putString("jobDescription", jobsList[index].jobDescription)

                args.putFloat("minSalary", jobsList[index].minimumSalary)
                args.putFloat("maxSalary", jobsList[index].maximumSalary)

                // args for getting job extra details
                args.putString("employerName", jobsList[index].employerName)
                args.putInt("employerId", jobsList[index].employerId)
                args.putInt("jobId", jobsList[index].jobId)

                parentFragment.findNavController()
                    .navigate(R.id.action_jobsFragment_to_jobDetailsFragment, args)

            }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle = itemView.findViewById<TextView>(R.id.entryJobTitle)
        val jobEmployerName = itemView.findViewById<TextView>(R.id.entryEmployerName)
        val jobDescription = itemView.findViewById<TextView>(R.id.entryJobDescription)

        val jobSalaryMin = itemView.findViewById<TextView>(R.id.entrySalaryMin)
        val jobSalaryMax = itemView.findViewById<TextView>(R.id.entrySalaryMax)
        // TODO - map other data of Job here as well (i.e salary, reviews, etc)
    }
}