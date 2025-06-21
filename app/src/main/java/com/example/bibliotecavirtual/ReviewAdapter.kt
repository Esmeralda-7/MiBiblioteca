package com.example.bibliotecavirtual

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val reviewList: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userNameTextView.text = review.userName
        holder.commentTextView.text = review.comment
        holder.ratingTextView.text = review.rating.toString()
    }

    override fun getItemCount(): Int = reviewList.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.user_name)
        val commentTextView: TextView = itemView.findViewById(R.id.comment)
        val ratingTextView: TextView = itemView.findViewById(R.id.rating)
    }
}