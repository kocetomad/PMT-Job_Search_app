package com.punchy.pmt.vacansee.recycleviewer

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.punchy.pmt.vacansee.JobsFragment
import com.punchy.pmt.vacansee.R
import kotlin.coroutines.coroutineContext


//input parameter has to be changed to an object containing the data from the query
class RvAdapter(val userList: ArrayList<String>,val parentFragment: Fragment) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.adapter_item_layout, p0, false)
        return ViewHolder(v);

    }
    override fun getItemCount(): Int {
        return userList.size
    }
    var isSaved = false
    fun setItemSaved(state: Boolean): Boolean {
        isSaved=state
        return isSaved
    }


    //the populates the view with the data from the query. Has to be changed to get the data fro mthe object not just a string
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        p0.name?.text = userList[p1].toString()
        p0.count?.text = userList[p1].toString()
        p0.itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card)?.setOnClickListener {
            println(p1)
            parentFragment.findNavController().navigate(R.id.action_jobsFragment_to_jobDetailsFragment)

        }

    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tvName)
        val count = itemView.findViewById<TextView>(R.id.tvCount)

    }
}