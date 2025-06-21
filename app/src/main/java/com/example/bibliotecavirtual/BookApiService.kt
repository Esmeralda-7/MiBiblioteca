package com.example.bibliotecavirtual
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApiService {

    @GET("/Libros/{id}/reviews")
    fun getReviews(@Path("id") bookId: Int): Call<List<Review>>

    @POST("/Libros/{id}/reviews")
    fun addReview(@Path("id") bookId: Int, @Body review: Review): Call<Void>

    @GET("/Libros")
    fun getBooks(@Query("page") page: Int, @Query("limit") limit: Int): Call<List<Book>>

    @GET("/Libros/{id}")
    fun find(@Path("id") id: Int): Call<Book>

    @GET("books")
    fun getBooksByCategory(@Query("category") category: String): Call<List<Book>>
}