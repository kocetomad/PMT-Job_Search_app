package com.punchy.pmt.vacansee.searchJobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.punchy.pmt.vacansee.R
import com.punchy.pmt.vacansee.searchJobs.httpRequests.getJobs


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var savedItems: MutableList<Int> = mutableListOf()
var touchDown = true


/**
 * A simple [Fragment] subclass.
 * Use the [JobsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JobsFragment : Fragment() {
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

        // add options menu support for action bar
        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.profileButton -> {
            findNavController().navigate(R.id.action_jobsFragment_to_profileFragment)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val trashBinIcon = resources.getDrawable(R.drawable.ic_baseline_archive_24, null)

        val jobsView: View = inflater.inflate(R.layout.fragment_jobs, container, false)

        val jobsForegroundView = jobsView.findViewById<LinearLayout>(R.id.jobsForegroundView)
        val bottomSheetBehavior = BottomSheetBehavior.from(jobsForegroundView)

        // set bottom sheet state as expanded by default
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Inflate the layout for this fragment
        val jobsRecyclerView = jobsView.findViewById<RecyclerView>(R.id.jobsRecyclerView)
//        Initializing the type of layout, here I have used LinearLayoutManager you can try GridLayoutManager
//        Based on your requirement to allow vertical or horizontal scroll , you can change it in  LinearLayout.VERTICAL
        jobsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )

        val jobsList = getJobs()

        if (jobsList.isEmpty()) {
            // get progress bar and hide it after the jobs load.
            jobsForegroundView.findViewById<ProgressBar>(R.id.jobsProgressBar).visibility =
                View.GONE

            // get error view and make it visible if the fetching fails
            jobsForegroundView.findViewById<TextView>(R.id.errorText).text =
                "Couldn't connect to endpoint"
            jobsForegroundView.findViewById<LinearLayout>(R.id.errorView).visibility = View.VISIBLE
        } else {
            // get progress bar and hide it after the jobs load.
            jobsForegroundView.findViewById<ProgressBar>(R.id.jobsProgressBar).visibility =
                View.GONE
        }

        /*jobsList.add(
            Job(
                jobId = 0,jobTitle = "Junior Software Engineer",
                jobDescription = "Job desc job desc job desc job desc",
                employerId = 0,
                employerName = "Berserk Electronics",
                employerProfileId = 0,
                employerProfileName = "placeholder",
                minimumSalary = 1500f, maximumSalary = 1500f,
                currency = "GBP",
                date="placeholder",
                expirationDate="placeholder",
                applications = 0,
                jobUrl = "placeholder",
                locationName = "placeholder"
            )
        )
        jobsList.add(
            Job(
                jobId = 0,jobTitle = "Hammer-Time worker",
                jobDescription = "Job desc job desc job desc job desc",
                employerId = 0,
                employerName = "The Old Fashion",
                employerProfileId = 0,
                employerProfileName = "placeholder",
                minimumSalary = 3500f, maximumSalary = 3500f,
                currency = "GBP",
                date="placeholder",
                expirationDate="placeholder",
                applications = 0,
                jobUrl = "placeholder",
                locationName = "placeholder"
            )
        )
        jobsList.add(
            Job(
                jobId = 0,jobTitle = "Professional Wanker",
                jobDescription = "Job desc job desc job desc job desc",
                employerId = 0,
                employerName = "WhoKnowsUs",
                employerProfileId = 0,
                employerProfileName = "placeholder",
                minimumSalary = 500f, maximumSalary = 1000f,
                currency = "GBP",
                date="placeholder",
                expirationDate="placeholder",
                applications = 0,
                jobUrl = "placeholder",
                locationName = "placeholder"
            )
        )*/

        val backdropTitle = jobsForegroundView.findViewById<TextView>(R.id.jobsBackdropTitle)
        backdropTitle.text = "Jobs found (${jobsList.size})"

//        pass the values to RvAdapter
        val rvAdapter = RvAdapter(jobsList, this)

//        set the recyclerView to the adapter
        jobsRecyclerView.adapter = rvAdapter

        jobsRecyclerView.setOnTouchListener({ v, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                println("Up")
                touchDown = false
            }
            if (MotionEvent.ACTION_DOWN == event.action) {
                println("Down")
                touchDown = true
            }
            false // return is important...
        })



        return jobsView
    }

    // TODO - Petrov - this function is not used. I just commented it out just in case.
    /*fun savedCheck(viewHolder: RvAdapter.ViewHolder){
        for(item in savedItems){
            if(viewHolder.adapterPosition == item){
                if(touchDown){
                    touchDown = false
                }
                //return
            }
        }
    }*/

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JobsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JobsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}