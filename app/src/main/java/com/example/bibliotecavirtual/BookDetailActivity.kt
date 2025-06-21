package com.example.bibliotecavirtual



import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliotecavirtual.R
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookDetailActivity : AppCompatActivity() {

    private lateinit var bookAverageRating: TextView
    private lateinit var reviewRecyclerView: RecyclerView
    private val reviewList = mutableListOf<Review>()
    private lateinit var reviewAdapter: ReviewAdapter
    private var averageRating: Float = 0f
    private lateinit var backButton: ImageButton
    private lateinit var botonFavorito: ImageButton
    private lateinit var leerButton: Button
    private var bookId: Int = -1
    private lateinit var bookTitle: String
    private lateinit var bookImageUrl: String
    private lateinit var bookDescription: String
    private lateinit var bookCategory: String
    private var bookUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        // Initialize favorite button
        botonFavorito = findViewById(R.id.BotonFavoritos)
        leerButton = findViewById(R.id.button_view_more)

        // Get data from Intent
        intent.extras?.let { bundle ->
            bookId = bundle.getInt("book_id", -1)
            bookTitle = bundle.getString("book_title", "")
            bookDescription = bundle.getString("book_description", "")
            bookImageUrl = bundle.getString("book_image_url", "")
            bookCategory = bundle.getString("book_category", "")
            bookUrl = bundle.getString("book_url")
        }

        // Setup views
        backButton = findViewById(R.id.Menu)
        val bookImageLarge = findViewById<ImageView>(R.id.book_image_large)
        val bookTitleLarge = findViewById<TextView>(R.id.book_title_large)
        val bookDescriptionLarge = findViewById<TextView>(R.id.book_description_large)
        val bookCategoryLarge = findViewById<TextView>(R.id.book_category_large)

        // Load image and text
        Glide.with(this).load(bookImageUrl).into(bookImageLarge)
        bookTitleLarge.text = bookTitle
        bookDescriptionLarge.text = bookDescription
        bookCategoryLarge.text = bookCategory

        // Setup favorite button click listener
        botonFavorito.setOnClickListener {
            guardarEnFavoritos(bookTitle, bookImageUrl, bookDescription, bookCategory, bookId)
        }
        backButton.setOnClickListener { finish() }

        // Initialize review views
        bookAverageRating = findViewById(R.id.book_average_rating)
        reviewRecyclerView = findViewById(R.id.review_recycler_view)
        reviewAdapter = ReviewAdapter(reviewList)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.adapter = reviewAdapter

        // Load book reviews
        loadBookReviews()

        // Setup read button
        leerButton.setOnClickListener { openBookUrl() }

        // Setup add review button
        findViewById<Button>(R.id.submit_review_button)?.setOnClickListener { showAddReviewDialog() }
    }

    private fun guardarEnFavoritos(title: String, imageUrl: String, description: String, category: String, id: Int) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val userId = user.uid
            val sharedPreferences = getSharedPreferences("favoritos", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val favoritos = HashSet(sharedPreferences.getStringSet("${userId}_favoritos", emptySet()) ?: emptySet())

            val libroFav = "$title|$imageUrl|$description|$category|$averageRating|$id|$bookUrl"
            favoritos.add(libroFav)
            editor.putStringSet("${userId}_favoritos", favoritos)
            editor.apply()

            Log.d("Favoritos", "Libro añadido a favoritos: $libroFav")
            Toast.makeText(this, "Libro añadido a favoritos", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Inicia sesión para añadir a favoritos", Toast.LENGTH_SHORT).show()
    }

    private fun openBookUrl() {
        bookUrl?.takeIf { it.isNotEmpty() }?.let {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        } ?: Toast.makeText(this, "URL del libro no disponible", Toast.LENGTH_SHORT).show()
    }

    private fun loadBookReviews() {
        val service = ApiClient.getClient("https://66da5709f47a05d55be492fd.mockapi.io/v1/").create(BookApiService::class.java)
        service.getReviews(bookId).enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful && response.body() != null) {
                    reviewList.clear()
                    reviewList.addAll(response.body()!!)
                    reviewAdapter.notifyDataSetChanged()
                    updateAverageRating()
                } else {
                    Toast.makeText(this@BookDetailActivity, "Error al cargar reseñas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Toast.makeText(this@BookDetailActivity, "Error de red al cargar reseñas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAverageRating() {
        averageRating = if (reviewList.isNotEmpty()) {
            reviewList.sumOf { it.rating.toDouble() }.toFloat() / reviewList.size
        } else {
            0f
        }
        bookAverageRating.text = String.format("%.1f", averageRating)
    }

    private fun showAddReviewDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)
        val reviewComment = dialogView.findViewById<EditText>(R.id.review_comment)
        val reviewRatingBar = dialogView.findViewById<RatingBar>(R.id.review_rating)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Agregar Reseña")
            .setPositiveButton("Enviar") { _, _ ->
                val comment = reviewComment.text.toString().trim()
                val rating = reviewRatingBar.rating
                val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
                val userName = preferences.getString("userEmail", "Usuario Anónimo")

                if (comment.isNotEmpty() && rating > 0) {
                    val newReview = Review("userId", userName ?: "Usuario Anónimo", comment, rating)
                    submitReview(newReview)
                } else {
                    Toast.makeText(this, "Por favor, agrega un comentario y una calificación.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun submitReview(review: Review) {
        if (bookId == -1) {
            Toast.makeText(this, "ID del libro no válido.", Toast.LENGTH_SHORT).show()
            return
        }

        val service = ApiClient.getClient("https://66da5709f47a05d55be492fd.mockapi.io/v1/").create(BookApiService::class.java)
        Log.d("BookDetailActivity", "Enviando reseña para bookId: $bookId")

        service.addReview(bookId, review).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@BookDetailActivity, "Reseña agregada exitosamente", Toast.LENGTH_SHORT).show()
                    loadBookReviews()
                } else {
                    Toast.makeText(this@BookDetailActivity, "Error al agregar la reseña. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BookDetailActivity, "Error de red al agregar la reseña", Toast.LENGTH_SHORT).show()
            }
        })
    }
}