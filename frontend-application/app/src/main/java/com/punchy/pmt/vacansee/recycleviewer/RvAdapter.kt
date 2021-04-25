package com.punchy.pmt.vacansee.recycleviewer

import com.punchy.pmt.vacansee.R

import android.graphics.ColorSpace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//input parameter has to be changed to an object containing the data from the query
class RvAdapter(val userList: ArrayList<String>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.adapter_item_layout, p0, false)
        return ViewHolder(v);
    }
    override fun getItemCount(): Int {
        return userList.size
    }
    var isSaved = false
    fun setItemSaved(state:Boolean): Boolean {
        isSaved=state
        return isSaved
    }

    //the populates the view with the data from the query. Has to be changed to get the data fro mthe object not just a string
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        p0.name?.text = userList[p1].toString()
        p0.count?.text = userList[p1].toString()
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tvName)
        val count = itemView.findViewById<TextView>(R.id.tvCount)


    }
}