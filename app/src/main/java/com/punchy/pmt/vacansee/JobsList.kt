package com.punchy.pmt.vacansee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class JobsList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs_list)

        supportActionBar?.elevation = 0F // to remove the drop shadow from the action bar

        // val backdropView = findViewById<LinearLayout>(R.id.backdropView)
        // val backdropSheetBehavior = BottomSheetBehavior.from(backdropView)

        /* to toggle the backdrop state use either
        backdropSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED
        or backdropSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED */

        // TODO add filters for job queries
    }
}