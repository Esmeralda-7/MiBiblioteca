package com.example.bibliotecavirtual

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FavoritesAdapter(private val favoriteBooks: List<Book>) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_book, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val book = favoriteBooks[position]
        holder.bookTitleTextView.text = book.title
        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .into(holder.bookCoverImageView)

        holder.bookCoverImageView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailActivity::class.java).apply {
                putExtra("book_title", book.title)
                putExtra("book_description", book.description)
                putExtra("book_image_url", book.imageUrl)
                putExtra("book_category", book.category)
                putExtra("book_url", book.bookUrl)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = favoriteBooks.size

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitleTextView: TextView = itemView.findViewById(R.id.book_titleF)
        val bookCoverImageView: ImageView = itemView.findViewById(R.id.book_imageF)
    }
}