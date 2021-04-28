package com.punchy.pmt.vacansee.searchJobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val trashBinIcon = resources.getDrawable(
            R.drawable.ic_baseline_archive_24,
            null
        )

        val jobsView: View = inflater.inflate(R.layout.fragment_jobs, container, false)

        val bottomSheetView = jobsView.findViewById<LinearLayout>(R.id.jobsBackdropView)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

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

        var jobsList = mutableListOf<Job>()

        try {
            jobsList = getJobs()

            // get progress bar and hide it after the jobs load.
            bottomSheetView.findViewById<ProgressBar>(R.id.jobsProgressBar).visibility = View.GONE
        } catch (e: Exception) {
            Log.e("JobsFragment", "FETCH ERROR: ")
            Log.e("JobsFragment", e.toString())

            // get error view and make it visible if the fetching fails
            bottomSheetView.findViewById<LinearLayout>(R.id.errorView).visibility = View.VISIBLE
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

        val backdropTitle = bottomSheetView.findViewById<TextView>(R.id.jobsBackdropTitle)
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


        var saveColor = Color.rgb(3f, 218f, 198f)
        val myCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                println(direction)
                // More code here
                touchDown = false
                rvAdapter?.notifyItemChanged(viewHolder.adapterPosition)
                savedItems.add(viewHolder.adapterPosition)
                Toast.makeText(context, "Job saved", Toast.LENGTH_SHORT).show()
                return
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // More code here
                for (item in savedItems) {
                    if (viewHolder.adapterPosition == item) {
                        if (touchDown) {
                            touchDown = false
                        }
                        return
                    }
                }
                c.clipRect(
                    15f, viewHolder.itemView.top.toFloat() + 20f,
                    dX + 50f, viewHolder.itemView.bottom.toFloat() - 20f
                )

                //making the save thingy change color
                if (218f + (dX / 5000) <= 255f && 198f + (dX / 5000) <= 255f) {
                    saveColor = Color.rgb(3f, 218f + (dX / 5000), 198f + (dX / 5000))
                }
                c.drawColor(saveColor)
                val textMargin = 100
                trashBinIcon.bounds = Rect(
                    textMargin,
                    viewHolder.itemView.top + (dX * 1.5).toInt() + textMargin,
                    textMargin + trashBinIcon.intrinsicWidth,
                    viewHolder.itemView.top + (dX * 1.5).toInt() + trashBinIcon.intrinsicHeight
                            + textMargin
                )
                trashBinIcon.draw(c)
                if (dX >= 214) {//SAVE THRESHOLD
                    if (touchDown) {
                        println("limit hit")
                        Toast.makeText(context, "Job saved", Toast.LENGTH_SHORT).show()
                        touchDown = false
                        rvAdapter?.notifyItemChanged(viewHolder.adapterPosition)
                        savedItems.add(viewHolder.adapterPosition)
                    }
                    return

                }
                super.onChildDraw(
                    c, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive
                )
            }


        }
        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(jobsRecyclerView)

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