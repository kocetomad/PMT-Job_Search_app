package com.punchy.pmt.vacansee.searchJobs.httpRequests

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.punchy.pmt.vacansee.searchJobs.Job
import com.punchy.pmt.vacansee.searchJobs.JobDetails
import com.punchy.pmt.vacansee.searchJobs.RvAdapter
import okhttp3.*
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


var route = "https://07fe6e8b59a6.ngrok.io/api"
private val client = OkHttpClient()
fun getJobs(): MutableList<Job> {
    val gson = Gson()
    var jobsList = mutableListOf<Job>()
    try {
        val url = URL("$route/jobs?search=developer")

        with(url.openConnection() as HttpsURLConnection) {
            requestMethod = "GET"  // optional default is GET
            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            val parseTemplate = object :
                TypeToken<MutableList<Job>>() {}.type //https://bezkoder.com/kotlin-parse-json-gson/
            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    println(line)
                    jobsList = gson.fromJson(line, parseTemplate)
                    jobsList.forEachIndexed { idx, tut -> println("> Item ${idx}:\n${tut}") }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("Requests", "Couldn't connect to endpoint")
        Log.e("Requests", e.toString())
    } finally {
        return jobsList
    }
}

fun getJobDetails(employerName: String, employerId: Int, jobId: Int): JobDetails {
    val gson = Gson()

    val url = URL("$route/moreDetails?empName=$employerName&empID=$employerId&jobID=$jobId")

    with(url.openConnection() as HttpsURLConnection) {
        requestMethod = "GET"  // optional default is GET
        println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

        val parseTemplate = object :
            TypeToken<MutableList<Job>>() {}.type //https://bezkoder.com/kotlin-parse-json-gson/
        inputStream.bufferedReader().use {
            it.lines().forEach { line ->
                println(line)
//                jobsList = gson.fromJson(line, parseTemplate)
//                jobsList.forEachIndexed { idx, tut -> println("> Item ${idx}:\n${tut}") }
            }
        }
    }

    // TODO - add proper details
    return JobDetails(0, 0, 0.0f)
}