package com.punchy.pmt.vacansee.searchJobs.httpRequests

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.punchy.pmt.vacansee.searchJobs.Job
import com.punchy.pmt.vacansee.searchJobs.JobDetails
import com.punchy.pmt.vacansee.sessionCookie
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


//var route = "https://138.68.133.230:5000"
const val route = "https://e128efdfc1a3.ngrok.io"
private val client = OkHttpClient()

fun login(email: String, password: String): Array<String?> {
    val formBody = FormBody.Builder()
        .add("email", email)
        .add("password", password)
        .build()

    val request = Request.Builder()
        .url("$route/api/login")
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Login Request begin:")
        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
        }


        // TODO - fetch login cookie
        val gson = Gson()
        val responseJSON = response.body!!.string()
//        val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type
        val cookie = response.headers.get("Set-Cookie")?.trim()?.split("\\s+".toRegex())//FORMATING THE COOKIE STRING SO ITS EASIER T OUSE FOR OUR PURPOUSES :))
        Log.d("login", "cookie:"+ (cookie?.get(0)?.dropLast(1)))
        Log.d("login", "req:"+JSONObject(responseJSON).get("msg").toString())
        Log.d("login", "req:"+JSONObject(responseJSON).get("success").toString())


        return arrayOf(JSONObject(responseJSON).get("success").toString(),(cookie?.get(0)?.dropLast(1)))
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

        Log.d("Requests","Logout Request begin:")
        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
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
    val formBody = FormBody.Builder()
        .add("username", username)
        .add("email", email)
        .add("password", password)
        .add("password2", password2)
        .add("firstName", firstName)
        .add("lastName", lastName)
        .add("dob","2000-12-12")
        .build()

    val request = Request.Builder()
        .url("$route/api/register")
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests","Register Account Request begin:")
        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
        }

        Log.d("Requests", response.body!!.string())
        // TODO - fetch login cookie
    }
}


fun getJobs(): MutableList<Job> {
    var jobsList: MutableList<Job>

    val request = Request.Builder()
        .url("$route/api/jobs?search=developer")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        for ((name, value) in response.headers) {
            Log.d("Requests","$name: $value")
        }

        val gson = Gson()
        val jobsJSON = response.body!!.string()
        val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type

        Log.d("Requests", jobsJSON)

        jobsList = gson.fromJson(JSONObject(jobsJSON).get("jobs").toString(), parseTemplate)
        return jobsList
    }
}

fun getJobDetails(employerName: String, employerId: Int, jobId: Int): JobDetails {
    val gson = Gson()

    val request = Request.Builder()
        .url("$route/api/moreDetails?empName=$employerName&empID=$employerId&jobID=$jobId")
        .addHeader("Cookie", sessionCookie)
        .build()

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