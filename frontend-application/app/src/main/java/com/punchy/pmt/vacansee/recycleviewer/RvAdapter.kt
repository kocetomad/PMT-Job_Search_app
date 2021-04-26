package com.punchy.pmt.vacansee.recycleviewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.punchy.pmt.vacansee.R

//input parameter has to be changed to an object containing the data from the query
class RvAdapter(val userList: ArrayList<String>, val parentFragment: Fragment) :
    RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(view: ViewGroup, index: Int): ViewHolder {
        val v =
            LayoutInflater.from(view?.context).inflate(R.layout.adapter_item_layout, view, false)
        return ViewHolder(v);

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    var isSaved = false
    fun setItemSaved(state: Boolean): Boolean {
        isSaved = state
        return isSaved
    }

    //the populates the view with the data from the query. Has to be changed to get the data from the object not just a string
    override fun onBindViewHolder(view: ViewHolder, index: Int) {
        view.name?.text = userList[index].toString()
        view.count?.text = userList[index].toString()
        view.itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card)
            ?.setOnClickListener {
                println(index)
                parentFragment.findNavController()
                    .navigate(R.id.action_jobsFragment_to_jobDetailsFragment)

            }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tvName)
        val count = itemView.findViewById<TextView>(R.id.tvCount)

    }
}