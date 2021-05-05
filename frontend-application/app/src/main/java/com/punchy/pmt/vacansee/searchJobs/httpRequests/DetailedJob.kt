package com.punchy.pmt.vacansee.searchJobs.httpRequests

class DetailedJob(
    val jobDetails: Job,
    val financeData: MutableList<FinanceData>,
    val reviewData: MutableList<ReviewData>
)