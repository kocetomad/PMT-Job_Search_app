package com.punchy.pmt.vacansee.searchJobs.httpRequests

class Job(
    val jobId: Int = -1,
    val jobTitle: String = "",
    val jobDescription: String = "",
    val employerId: Int = -1,
    val employerName: String = "",
    val employerProfileId: Int = -1,
    val employerProfileName: String = "",
    val locationName: String = "",
    val salaryType: String = "",
    val minimumSalary: Float = -1f,
    val maximumSalary: Float = -1f,
    val yearlyMinimumSalary: Float = -1f,
    val yearlyMaximumSalary: Float = -1f,
    val currency: String = "",
    val datePosted: String = "", // TODO - check if this breaks "date" from /jobs
    val expirationDate: String = "",
    val date: String = "",
    val applicationCount: Int = -1,
    val jobUrl: String = "",
    val externalUrl: String = "",
    val partTime: Boolean = false,
    val fullTime: Boolean = false,
    val contractType: String = "",
    val logoUrl: String = ""
) {
    override fun toString(): String {
        return "JobSummary [ID: ${this.jobId}, employerId: ${this.employerId}, employerName: ${this.employerName}]"
    }
    var saved = false
    fun setSaved(){
        saved = true
    }
}