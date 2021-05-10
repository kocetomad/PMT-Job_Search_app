package com.punchy.pmt.vacansee.searchJobs.detailedJob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.punchy.pmt.vacansee.R
import com.punchy.pmt.vacansee.searchJobs.httpRequests.ReviewData
import com.punchy.pmt.vacansee.searchJobs.jobsList

class ReviewsRvAdapter(private val reviewsList: MutableList<ReviewData>) :
    RecyclerView.Adapter<ReviewsRvAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTitle = itemView.findViewById<TextView>(R.id.reviewTitle)
        val reviewDescription = itemView.findViewById<TextView>(R.id.reviewDescription)
        val reviewScoreText = itemView.findViewById<TextView>(R.id.reviewScore)

        val reviewStar1 = itemView.findViewById<ImageView>(R.id.reviewStar1)
        val reviewStar2 = itemView.findViewById<ImageView>(R.id.reviewStar2)
        val reviewStar3 = itemView.findViewById<ImageView>(R.id.reviewStar3)
        val reviewStar4 = itemView.findViewById<ImageView>(R.id.reviewStar4)
        val reviewStar5 = itemView.findViewById<ImageView>(R.id.reviewStar5)
    }

    override fun onCreateViewHolder(view: ViewGroup, index: Int): ViewHolder {
        val v = LayoutInflater.from(view.context).inflate(R.layout.review_entry_layout, view, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }

    override fun onBindViewHolder(view: ViewHolder, index: Int) {
        view.reviewTitle?.text = reviewsList[index].title
        view.reviewDescription?.text = reviewsList[index].description
        view.reviewScoreText?.text = "${reviewsList[index].rating} out of 5.0"

        when {
            reviewsList[index].rating > 4.5f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar4.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar5.setImageResource(R.drawable.ic_baseline_star_24)
            }
            reviewsList[index].rating > 4.0f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar4.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar5.setImageResource(R.drawable.ic_baseline_star_half_24)
            }
            reviewsList[index].rating > 3.5f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar4.setImageResource(R.drawable.ic_baseline_star_24)
            }
            reviewsList[index].rating > 3.0f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar4.setImageResource(R.drawable.ic_baseline_star_half_24)
            }
            reviewsList[index].rating > 2.5f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar3.setImageResource(R.drawable.ic_baseline_star_24)
            }
            reviewsList[index].rating > 2.0f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar3.setImageResource(R.drawable.ic_baseline_star_half_24)
            }
            reviewsList[index].rating > 1.5f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_24)
            }
            reviewsList[index].rating > 1.0f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
                view.reviewStar2.setImageResource(R.drawable.ic_baseline_star_half_24)
            }
            reviewsList[index].rating > 0.5f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_24)
            }
            reviewsList[index].rating > 0.0f -> {
                view.reviewStar1.setImageResource(R.drawable.ic_baseline_star_half_24)
            }
        }
    }
}