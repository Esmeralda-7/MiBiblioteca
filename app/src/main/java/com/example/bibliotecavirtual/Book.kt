package com.example.bibliotecavirtual

import com.google.gson.annotations.SerializedName

data class Book(
    val title: String,
    val description: String,
    val imageUrl: String,
    val autor: String,
    val category: String,
    @SerializedName("averageRating") val averageRating: Float,
    val reviews: List<Review>?,
    val id: Int,
    val bookUrl: String?
)