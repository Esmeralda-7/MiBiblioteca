package com.example.bibliotecavirtual

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class Favorites : AppCompatActivity() {

    private lateinit var recyclerViewFavorites: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private val favoriteBooks = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_books)

        recyclerViewFavorites = findViewById(R.id.list_favorites)
        recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        favoritesAdapter = FavoritesAdapter(favoriteBooks)
        recyclerViewFavorites.adapter = favoritesAdapter

        mostrarFavoritos()
    }

    private fun mostrarFavoritos() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val userId = user.uid
            val sharedPreferences = getSharedPreferences("favoritos", MODE_PRIVATE)
            val favoritos = sharedPreferences.getStringSet("${userId}_favoritos", emptySet()) ?: emptySet()

            Log.d("Favoritos", "Favoritos cargados: $favoritos")
            favoriteBooks.clear()

            favoritos.takeIf { it.isNotEmpty() }?.forEach { favorito ->
                val datosLibro = favorito.split("|")
                if (datosLibro.size == 7) {
                    try {
                        val title = datosLibro[0]
                        val imageUrl = datosLibro[1]
                        val description = datosLibro[2]
                        val category = datosLibro[3]
                        val averageRating = datosLibro[4].toFloat()
                        val id = datosLibro[5].toInt()
                        val bookUrl = datosLibro[6]

                        val book = Book(title, "", description, imageUrl, category, averageRating, emptyList(), id, bookUrl)
                        favoriteBooks.add(book)
                    } catch (e: NumberFormatException) {
                        Log.w("Favoritos", "Error al convertir datos de libro: $favorito", e)
                    }
                } else {
                    Log.w("Favoritos", "Formato de datos incorrecto para favorito: $favorito")
                }
            }

            if (favoriteBooks.isNotEmpty()) {
                favoritesAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No tienes libros en favoritos", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Inicia sesión para ver tus favoritos", Toast.LENGTH_SHORT).show()
    }
}