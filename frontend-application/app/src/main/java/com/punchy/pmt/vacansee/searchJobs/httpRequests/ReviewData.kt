package com.punchy.pmt.vacansee.searchJobs.httpRequests

class ReviewData(
    val employer_id: Int = 0,
    val user_id: Int = 0,
    val rating: Float = 0f,
    val title: String = "",
    val description: String = ""
) {
    override fun toString(): String {
        return "{title: ${this.title}, rating: ${this.rating}}"
    }
}