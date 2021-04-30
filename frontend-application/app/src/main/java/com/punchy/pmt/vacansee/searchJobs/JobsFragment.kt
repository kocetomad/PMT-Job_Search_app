package com.punchy.pmt.vacansee.searchJobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.punchy.pmt.vacansee.R
import java.net.URL
import javax.net.ssl.HttpsURLConnection


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
        val trashBinIcon = resources.getDrawable(
            R.drawable.ic_baseline_archive_24,
            null
        )

        val jobsView: View = inflater.inflate(R.layout.fragment_jobs, container, false)

        val backdropView = jobsView.findViewById<LinearLayout>(R.id.jobsBackdropView)
        val bottomSheetBehavior = BottomSheetBehavior.from(backdropView)

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
        val gson = Gson()//Json converter
        var jobsList = mutableListOf<Job>()
        fun sendGet() {
            val url = URL("https://56bea244b45d.ngrok.io/api/jobs?search=developer")

            with(url.openConnection() as HttpsURLConnection) {
                requestMethod = "GET"  // optional default is GET
                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type //https://bezkoder.com/kotlin-parse-json-gson/
                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        println(line)
                        jobsList = gson.fromJson(line, parseTemplate)
                        jobsList.forEachIndexed  { idx, tut -> println("> Item ${idx}:\n${tut}") }
                    }
                }
            }
        }
        sendGet()





//        Create an arraylist
//        jobsList.add(Job("Junior Software Engineer", "Berzerker Electronics", 1500f, "Job desc job desc job desc job desc"))
//        jobsList.add(Job("Hammer-Time worker", "The Old Fashion", 3500f, "Job desc job desc job desc job desc"))
//        jobsList.add(Job("Professional Wanker", "WhoKnowsUs", 500f, "Job desc job desc job desc job desc"))

        val backdropTitle = backdropView.findViewById<TextView>(R.id.jobsBackdropTitle)
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
        val saveColor = Color.parseColor("#03DAC6")
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

                c.drawColor(saveColor)
                val textMargin = 100
                trashBinIcon.bounds = Rect(
                    textMargin,
                    viewHolder.itemView.top + textMargin,
                    textMargin + trashBinIcon.intrinsicWidth,
                    viewHolder.itemView.top + trashBinIcon.intrinsicHeight
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