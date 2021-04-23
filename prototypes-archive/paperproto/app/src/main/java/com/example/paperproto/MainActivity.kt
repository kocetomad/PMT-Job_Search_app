package com.example.paperproto

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val trashBinIcon = resources.getDrawable(
                R.drawable.ic_baseline_archive_24,
                null
        )
        super.onCreate(savedInstanceState)
//        Set layout file to class
        setContentView(R.layout.activity_main)
//        initialize the recyclerView from the XML
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//        Initializing the type of layout, here I have used LinearLayoutManager you can try GridLayoutManager
//        Based on your requirement to allow vertical or horizontal scroll , you can change it in  LinearLayout.VERTICAL
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        Create an arraylist
        val dataList = ArrayList<String>()
        dataList.add("aasdas")
        dataList.add("Asdasd")
        dataList.add("ColorSpace.Model(")
        dataList.add("4656666665")
//        pass the values to RvAdapter
        val rvAdapter = RvAdapter(dataList)
//        set the recyclerView to the adapter
        recyclerView.adapter = rvAdapter;

        val myCallback = object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                  direction: Int) {

                // More code here
                rvAdapter?.notifyItemRemoved(viewHolder.adapterPosition)

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

                c.clipRect(0f, viewHolder.itemView.top.toFloat(),
                        dX+8, viewHolder.itemView.bottom.toFloat())

                    c.drawColor(Color.GREEN)
                val textMargin = 100
                trashBinIcon.bounds = Rect(
                        textMargin,
                        viewHolder.itemView.top + textMargin,
                        textMargin + trashBinIcon.intrinsicWidth,
                        viewHolder.itemView.top + trashBinIcon.intrinsicHeight
                                + textMargin
                )
                trashBinIcon.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive)
            }


        }
        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(recyclerView)


    }




}


