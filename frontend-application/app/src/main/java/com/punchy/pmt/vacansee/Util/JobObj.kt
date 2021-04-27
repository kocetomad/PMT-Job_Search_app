package com.punchy.pmt.vacansee.Util

class JobObj(
    val jobId: String,
    val employerId: String,
    val employerName: String,
    val employerProfileId: String,
    val employerProfileName: String,
    val jobTitle: String,
    val locationName: String,
    val minimumSalary: Int,
    val maximumSalary: Int,
    val currency: String,
    val expirationDate: String,
    val date: String,
    val jobDescription: String,
    val applications: Int,
    val jobUrl: String
) {
    override fun toString(): String {
        return "JobSummary [ID: ${this.jobId}, employerId: ${this.employerId}, employerName: ${this.employerName}]"
    }
}