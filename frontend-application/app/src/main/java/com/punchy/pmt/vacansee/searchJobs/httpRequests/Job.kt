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
    val salaryType: String,
    val minimumSalary: Float,
    val maximumSalary: Float,
    val yearlyMinimumSalary: Float,
    val yearlyMaximumSalary: Float,
    val currency: String,
    val datePosted: String, // TODO - check if this breaks "date" from /jobs
    val expirationDate: String,
    val date: String?,
    val applicationCount: Int,
    val jobUrl: String,
    val externalUrl: String,
    val partTime: Boolean,
    val fullTime: Boolean,
    val contractType: String,
) {
    override fun toString(): String {
        return "JobSummary [ID: ${this.jobId}, employerId: ${this.employerId}, employerName: ${this.employerName}]"
    }
    var saved = false
    fun setSaved(){
        saved = true
    }
}