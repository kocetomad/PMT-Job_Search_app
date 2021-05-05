package com.punchy.pmt.vacansee.searchJobs.httpRequests

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.punchy.pmt.vacansee.sessionCookie
import com.punchy.pmt.vacansee.userID
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


const val route = "https://www.pmtjobapp.xyz"
private val client = OkHttpClient()
// val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type
// put here just in case it's ever needed in the future


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


        val responseJSON = response.body!!.string()
        val cookie = response.headers.get("Set-Cookie")?.trim()
            ?.split("\\s+".toRegex()) // Formatting the cookie string so it's easier to use for our purposes :))
        Log.d("login", "cookie:" + (cookie?.get(0)?.dropLast(1)))
        Log.d("login", "userID:" + JSONObject(responseJSON).get("userID").toString())
        Log.d("login", "req:" + JSONObject(responseJSON).get("msg").toString())
        Log.d("login", "req:" + JSONObject(responseJSON).get("success").toString())


        return arrayOf(
            JSONObject(responseJSON).get("success").toString(),
            (cookie?.get(0)?.dropLast(1)),
            JSONObject(responseJSON).get("userID").toString()
        )
    }
}


fun logout() {
    val request = Request.Builder()
        .url("$route/logout")
        // TODO - add auth cookie on logout
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Logout Request begin:")
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
        .add("dob", "2000-12-12")
        .build()

    val request = Request.Builder()
        .url("$route/api/register")
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Register Account Request begin:")
        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
        }

        Log.d("Requests", response.body!!.string())
    }
}


fun getJobs(searchParam: String): MutableList<Job> {
    var jobsList: MutableList<Job>

    val request = Request.Builder()
        .url("$route/api/jobs?search=$searchParam")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val gson = Gson()
        val jobsJSON = response.body!!.string()
        val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type

        jobsList = gson.fromJson(JSONObject(jobsJSON).get("jobs").toString(), parseTemplate)

        Log.d("Requests - getJobs", jobsList.toString())
        return jobsList
    }
}


fun getJobDetails(employerName: String, employerId: Int, jobId: Int): DetailedJob {
    var details: Job
    var reviews: MutableList<ReviewData>
    var financeData: MutableList<FinanceData>

    val request = Request.Builder()
        .url("$route/api/moreDetails?empName=$employerName&empID=$employerId&jobID=$jobId")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
        }

        val job = response.body!!.string()

        Log.d("Requests - jobDetails", JSONObject(job).toString())

        val jobDetailsParseTemplate = object : TypeToken<Job>() {}.type

        val detailsDataString =
            JSONObject(job).get("jobDetails").toString().removePrefix("[").removeSuffix("]")
        details = Gson().fromJson(detailsDataString, jobDetailsParseTemplate)

        val reviewsParseTemplate = object : TypeToken<MutableList<ReviewData>>() {}.type
        val reviewsDataString = JSONObject(job).get("reviewData").toString()
        reviews = Gson().fromJson(reviewsDataString, reviewsParseTemplate)

        val financeParseTemplate = object : TypeToken<MutableList<FinanceData>>() {}.type
        val financeDataString = JSONObject(job).get("financeData").toString()
        financeData = Gson().fromJson(financeDataString, financeParseTemplate)

        Log.d("Requests - jobDetails", details.toString())
        Log.d("Requests - reviews", reviews.toString())
        Log.d("Requests - financeData", financeData.toString())

        return DetailedJob(details, financeData, reviews)
    }
}


fun getSavedJobs(): MutableList<Job> {
    val savedList: MutableList<Job>

    val request = Request.Builder()
        .url("$route/api/pinned?user=$userID")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Request begin:")
        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
        }

        val gson = Gson()
        val jobsJSON = response.body!!.string()
        val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type
        Log.d("SavedRequests", "job $jobsJSON")

        savedList = gson.fromJson(JSONObject(jobsJSON).get("jobs").toString(), parseTemplate)
        Log.d("SavedRequests", "job $jobsJSON")

        return savedList
    }
}


fun saveJob(jobID: String): Array<String?> {
    val formBody = FormBody.Builder()
        .add("userID", userID)
        .add("jobID", jobID)
        .build()

    val request = Request.Builder()
        .url("$route/api/pinned")
        .addHeader("Cookie", sessionCookie)
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Request begin:")
        for ((name, value) in response.headers) {
            Log.d("Requests", "$name: $value")
        }

        val responseJSON = response.body!!.string()

        Log.d("save", "req:" + JSONObject(responseJSON).get("msg").toString())
        Log.d("save", "req:" + JSONObject(responseJSON).get("success").toString())

        return arrayOf(
            JSONObject(responseJSON).get("success").toString(),
            JSONObject(responseJSON).get("msg").toString()
        )
    }
}