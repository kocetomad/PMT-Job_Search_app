package com.punchy.pmt.vacansee.searchJobs.httpRequests

class Job(
    val jobId: Int,
    val jobTitle: String,
    val jobDescription: String,
    val employerId: Int,
    val employerName: String,
    val employerProfileId: Int,
    val employerProfileName: String,
    val locationName: String,
    val minimumSalary: Float,
    val maximumSalary: Float,
    val currency: String,
    val expirationDate: String,
    val date: String?,
    val applications: Int,
    val jobUrl: String
) {
    override fun toString(): String {
        return "JobSummary [ID: ${this.jobId}, employerId: ${this.employerId}, employerName: ${this.employerName}]"
    }
}