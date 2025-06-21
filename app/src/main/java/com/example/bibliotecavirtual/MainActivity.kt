package com.example.bibliotecavirtual

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CATEGORY_REQUEST_CODE = 1
        private const val PAGE_SIZE = 10
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var recommendedRecyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var recommendedAdapter: BookAdapter
    private val bookList = mutableListOf<Book>()
    private val filteredBookList = mutableListOf<Book>()
    private val recommendedBookList = mutableListOf<Book>()
    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize book lists
        bookList.clear()
        filteredBookList.clear()
        recommendedBookList.clear()

        // Setup RecyclerView for all books
        recyclerView = findViewById(R.id.book_list)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        bookAdapter = BookAdapter(filteredBookList, object : BookAdapter.OnItemClickListener {
            override fun onItemClick(book: Book) {
                openBookDetailActivity(book)
            }
        })
        recyclerView.adapter = bookAdapter

        // Setup RecyclerView for recommended books
        recommendedRecyclerView = findViewById(R.id.recommended_book_list)
        recommendedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recommendedAdapter = BookAdapter(recommendedBookList, object : BookAdapter.OnItemClickListener {
            override fun onItemClick(book: Book) {
                openBookDetailActivity(book)
            }
        })
        recommendedRecyclerView.adapter = recommendedAdapter

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                if (layoutManager.findLastVisibleItemPosition() >= filteredBookList.size - 1) {
                    fetchBooksFromAPI()
                }
            }
        })

        // Fetch initial books
        fetchBooksFromAPI()

        // Setup category button
        findViewById<Button>(R.id.button).setOnClickListener { openCategoryActivity() }

        // Setup SearchView
        findViewById<SearchView>(R.id.search_view).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterBooks(newText.orEmpty())
                return true
            }
        })

        // Setup favorites button
        findViewById<ImageButton>(R.id.ButtonF).setOnClickListener { openFavoritesActivity() }
    }

    private fun openBookDetailActivity(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("book_id", book.id)
            putExtra("book_title", book.title)
            putExtra("book_autor", book.autor)
            putExtra("book_description", book.description)
            putExtra("book_image_url", book.imageUrl)
            putExtra("book_category", book.category)
            putExtra("book_url", book.bookUrl)
        }
        startActivity(intent)
    }

    private fun openCategoryActivity() {
        startActivityForResult(Intent(this, CategoryActivity::class.java), CATEGORY_REQUEST_CODE)
    }

    private fun openFavoritesActivity() {
        startActivity(Intent(this, Favorites::class.java))
    }

    private fun fetchBooksFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://66da5709f47a05d55be492fd.mockapi.io/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(BookApiService::class.java)

        service.getBooks(currentPage, PAGE_SIZE).enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful && response.body() != null) {
                    val newBooks = response.body()!!
                    bookList.addAll(newBooks)
                    filteredBookList.addAll(newBooks)
                    currentPage++

                    // Obtener reseñas para cada libro
                    newBooks.forEach { book ->
                        fetchReviewsForBook(book, service)
                    }

                    guardarLibrosOffline(bookList)
                } else {
                    Log.e("Main_APP", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Book>>, throwable: Throwable) {
                Log.e("Main_APP", "Error en la solicitud: ${throwable.message}")
            }
        })
    }
    private fun fetchReviewsForBook(book: Book, service: BookApiService) {
        service.getReviews(book.id).enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful && response.body() != null) {
                    val reviews = response.body()!!
                    // Calculate the averageRating
                    val averageRating = if (reviews.isNotEmpty()) {
                        reviews.sumOf { it.rating.toDouble() }.toFloat() / reviews.size
                    } else {
                        0f
                    }

                    // Update the book with the new averageRating and reviews
                    val updatedBook = book.copy(
                        averageRating = averageRating,
                        reviews = reviews // Set the reviews from the API response
                    )

                    // Update bookList
                    val indexInBookList = bookList.indexOfFirst { it.id == book.id }
                    if (indexInBookList != -1) {
                        bookList[indexInBookList] = updatedBook
                    }

                    // Update filteredBookList
                    val indexInFilteredList = filteredBookList.indexOfFirst { it.id == book.id }
                    if (indexInFilteredList != -1) {
                        filteredBookList[indexInFilteredList] = updatedBook
                    }

                    // Refresh adapters to update the UI
                    refreshAdapters()
                } else {
                    Log.e("Main_APP", "Error fetching reviews for book ${book.id}: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Log.e("Main_APP", "Network error fetching reviews for book ${book.id}: ${t.message}")
            }
        })
    }

    private fun guardarLibrosOffline(books: List<Book>) {
        val sharedPreferences = getSharedPreferences("libros_offline", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val libros = HashSet<String>()

        books.forEach { book ->
            val libroData = "${book.title}|${book.imageUrl}|${book.description}|${book.category}|${book.averageRating}|${book.id}"
            libros.add(libroData)
        }

        editor.putStringSet("lista_libros", libros)
        editor.apply()
    }

    private fun refreshAdapters() {
        bookAdapter.notifyDataSetChanged()
        recommendedAdapter.notifyDataSetChanged()
    }

    private fun filterBooks(query: String) {
        filteredBookList.clear()
        if (query.isEmpty()) {
            filteredBookList.addAll(bookList)
        } else {
            bookList.filterTo(filteredBookList) { it.title.lowercase().contains(query.lowercase()) }
        }
        bookAdapter.notifyDataSetChanged()
    }

    private fun filterBooksByCategory(category: String?) {
        filteredBookList.clear()
        if (category == null) {
            filteredBookList.addAll(bookList)
        } else {
            bookList.filterTo(filteredBookList) { it.category == category }
        }
        bookAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedCategory = data.getStringExtra("selected_category")
            filterBooksByCategory(selectedCategory)
        }
    }
}