package com.punchy.pmt.vacansee

import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import com.punchy.pmt.vacansee.searchJobs.JobsFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}