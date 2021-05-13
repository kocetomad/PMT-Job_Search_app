package com.punchy.pmt.vacansee.searchJobs.httpRequests

class Profile(
    val first_name: String = "",
    val last_name: String = "",
    val email: String = "",
    val dob: String = "",
    val profile_url: String = ""
) {
    override fun toString(): String {
        return "[{firstName: ${this.first_name}, lastName: ${this.last_name}, email: ${this.email}, profileUrl: ${this.profile_url}}]"
    }
}