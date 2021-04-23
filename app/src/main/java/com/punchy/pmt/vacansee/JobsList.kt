package com.punchy.pmt.vacansee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import okhttp3.Response

class JobsList : AppCompatActivity() {
    private val client = OkHttpClient()

    private fun getReedJobs() {
        val request = Request.Builder()
            .url("https://www.reed.co.uk/api/1.0/search?locationName=bournemouth")
            .addHeader("Authorization", "Basic ODAxMDc2ZGEtYTViYi00YjhlLWE5YTUtZDVkNDJiMDQ3OWYwOg==")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    /*for ((name, value) in response.headers) {
                        Log.d("JobsList - headers","$name: $value")
                    }*/

                    Log.d("JobsList - body", response.body!!.string())
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs_list)

        // is null safe because the activity may not have an action bar defined.
        supportActionBar?.elevation = 0F // to remove the drop shadow from the action bar

        // val backdropView = findViewById<LinearLayout>(R.id.backdropView)
        // val backdropSheetBehavior = BottomSheetBehavior.from(backdropView)

        /* to toggle the backdrop state use either
        backdropSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED
        or backdropSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED */

        // TODO add filters for job queries

        val getJobsButton = findViewById<Button>(R.id.getJobsButton)
        getJobsButton.setOnClickListener {
            getReedJobs()
        }
    }
}