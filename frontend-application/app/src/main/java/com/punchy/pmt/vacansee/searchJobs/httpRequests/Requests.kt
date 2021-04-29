package com.punchy.pmt.vacansee.searchJobs.httpRequests

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.punchy.pmt.vacansee.searchJobs.Job
import com.punchy.pmt.vacansee.searchJobs.JobDetails
import okhttp3.*
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


var route = "https://6aaf014ab2d0.ngrok.io/"
private val client = OkHttpClient()

fun getJobs(): MutableList<Job> {
    var jobsList: MutableList<Job>

    val request = Request.Builder()
        .url("$route/api/jobs?search=developer")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        for ((name, value) in response.headers) {
            println("$name: $value")
        }

        val gson = Gson()
        val jobsJSON = response.body!!.string()
        val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type

        jobsList = gson.fromJson(jobsJSON, parseTemplate)
        jobsList.forEachIndexed { idx, tut ->
            println("> Item ${idx}:\n${tut}")
        }

        return jobsList
    }
}

fun getJobDetails(employerName: String, employerId: Int, jobId: Int): JobDetails {
    val gson = Gson()

    val url = URL("$route/api/moreDetails?empName=$employerName&empID=$employerId&jobID=$jobId")

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

fun getSavedJobs(): MutableList<Job> {
    return mutableListOf()
}

fun login(email: String, password: String) {
    val request = Request.Builder()
        .url("$route/login")
        .addHeader("email", email)
        .addHeader(password, "password")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        for ((name, value) in response.headers) {
            println("$name: $value")
        }
        // TODO - fetch login cookie
    }
}

fun logout() {
    // TODO - add auth cookie on logout
    val request = Request.Builder()
        .url("$route/logout")
        // TODO - add auth cookie
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        for ((name, value) in response.headers) {
            println("$name: $value")
        }
    }
}

fun registerAccount(
    username: String,
    email: String,
    password: String,
    password2: String,
    firstName: String,
    lastName: String
    // TODO - add Date of Birth
) {

}