package com.punchy.pmt.vacansee.searchJobs.httpRequests

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.punchy.pmt.vacansee.sessionCookie
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


const val route = "https://www.pmtjobapp.xyz"
private val client = OkHttpClient()
// val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type
// put here just in case it's ever needed in the future

/* Might be worth to add to all requests a "code" attribute which is returned by the backend
*   so for example you have arrayOf("200", jobsList)
*   and you can do
*   if(response[0] == "200") {
*     proceed with rest of code
*   } else if (response[0] == "404") {
*     show a toast user not found
*   } */

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
//        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        if (!response.isSuccessful) {
            return arrayOf("false", "", "")
        }

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
    lastName: String,
    dateOfBirth: String // example 2000-12-12 / year-month-day
) {
    val formBody = FormBody.Builder()
        .add("username", username)
        .add("email", email)
        .add("password", password)
        .add("password2", password2)
        .add("firstName", firstName)
        .add("lastName", lastName)
        .add("dob", dateOfBirth)
        .build()

    val request = Request.Builder()
        .url("$route/api/register")
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Register Account Request begin:")
        Log.d("Requests", response.body!!.string())
    }
}


fun getJobs(
    searchParam: String?,
    locationParam: String?,
    partTime: Boolean?,
    fullTime: Boolean?,
    locationDistance: Int,
    minSalary: Int,
    maxSalary: Int
): List<String> {
    var searchParamEnd = "job" // uses "job" as placeholder
    if (searchParam != null) {
        searchParamEnd = searchParam
    }

    var jobsList: MutableList<Job>

    var locationText = ""
    if (!locationParam.isNullOrEmpty()) {
        locationText = "&location=$locationParam"
    }

    val partTimeText: String = if (partTime == true) {
        "&partTime=true"
    } else
        "&partTime=false"

    val fullTimeText: String = if (fullTime == true) {
        "&fullTime=true"
    } else {
        "&fullTime=false"
    }


    val request = Request.Builder()
        .url(
            "$route/api/jobs?search=$searchParamEnd" +
                    locationText +
                    partTimeText +
                    fullTimeText +
                    "&distance=$locationDistance" +
                    "&minimumSalary=$minSalary" +
                    "&maximumSalary=$maxSalary"
        )
        .addHeader("Cookie", sessionCookie)
        .build()

    Log.d("Requests - getJobs", "URL: ${request.url}")

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Requests", response.toString())

            return listOf(response.code.toString(), "")
            // throw IOException("Unexpected code $response")
        } else {
            val gson = Gson()
            val jobsJSON = response.body!!.string()

            val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type
            jobsList = gson.fromJson(JSONObject(jobsJSON).get("jobs").toString(), parseTemplate)
            Log.d("Requests - getJobs", jobsList.toString())

            return listOf(response.code.toString(), JSONObject(jobsJSON).get("jobs").toString())
        }
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
    var savedList = mutableListOf<Job>()

    val request = Request.Builder()
        .url("$route/api/pinned")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Requests - getSavedJobs", "Unexpected code $response")
            return savedList
        }

        Log.d("Requests", "Request \"getSavedJobs\" begin:")

        val gson = Gson()
        val jobsJSON = response.body!!.string()
        val parseTemplate = object : TypeToken<MutableList<Job>>() {}.type

        try {
            savedList = gson.fromJson(JSONObject(jobsJSON).get("jobs").toString(), parseTemplate)

        } catch (e: Exception) {
            Log.d("SavedRequests", "can't get jobs: $e")
        }
        Log.d("SavedRequests", "count" + savedList.size)

        return savedList
    }
}


fun saveJob(jobID: String): Array<String?> {
    val formBody = FormBody.Builder()
        .add("jobID", jobID)
        .build()

    val request = Request.Builder()
        .url("$route/api/pinned")
        .addHeader("Cookie", sessionCookie)
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("Requests", "Request \"saveJob\" begin:")

        val responseJSON = response.body!!.string()

        Log.d("save", "req:" + JSONObject(responseJSON).get("msg").toString())
        Log.d("save", "req:" + JSONObject(responseJSON).get("success").toString())

        return arrayOf(
            JSONObject(responseJSON).get("success").toString(),
            JSONObject(responseJSON).get("msg").toString()
        )
    }
}

fun unpinJob(jobID: String): Array<String?> {
    Log.d("unpin", "IN")

    val formBody = FormBody.Builder()
        .add("jobID", jobID)
        .build()

    val request = Request.Builder()
        .url("$route/api/pinned")
        .addHeader("Cookie", sessionCookie)
        .delete(formBody)
        .build()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            Log.d("Requests", "Request \"unpin\" begin:")

            val responseJSON = response.body!!.string()

            Log.d("unpin", "req:" + JSONObject(responseJSON).get("msg").toString())
            Log.d("unpin", "req:" + JSONObject(responseJSON).get("success").toString())

            return arrayOf(
                JSONObject(responseJSON).get("success").toString(),
                JSONObject(responseJSON).get("msg").toString()
            )
        }
    } catch (e: Exception) {
        Log.d("unpin", "req:" + e)
    }

    return arrayOf(
        "JSONObject(responseJSON).get(",
        ""
    )
}

fun getProfile(): List<String> {
    val request = Request.Builder()
        .url("$route/api/profile")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Requests - getProfile", response.toString())
            return listOf(response.code.toString(), Profile().toString())
        }

        Log.d("Requests - getProfile", response.toString())
//            throw IOException("Unexpected code $response")

        val responseJSON = response.body!!.string()
        Log.d("Requests - getProfile", responseJSON)

        return listOf(
            JSONObject(responseJSON).get("success").toString(),
            JSONObject(responseJSON).get("profile").toString()
        )
    }
}

fun postReview(
    employerID: Int,
    reviewTitle: String,
    reviewRating: Float,
    reviewDescription: String
): Int {
    val formBody = FormBody.Builder()
        .add("empID", employerID.toString())
        .add("rating", reviewRating.toString()) // may throw errors if it's float
        .add("title", reviewTitle)
        .add("desc", reviewDescription)
        .build()

    val request = Request.Builder()
        .url("$route/api/review")
        .addHeader("Cookie", sessionCookie)
        .post(formBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Requests - postReview", response.toString())
            return response.code
        }

        Log.d("Requests - postReview", response.body.toString())
        return 200
    }
}

fun getReview(employerID: Int): ReviewData {
    val request = Request.Builder()
        .url("$route/api/review")
        .addHeader("Cookie", sessionCookie)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Requests - postReview", response.toString())
        }

        Log.d("Requests - postReview", response.body.toString())
    }

    return ReviewData()
}