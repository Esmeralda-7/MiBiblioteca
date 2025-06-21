package com.example.bibliotecavirtual

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecavirtual.R
import com.google.firebase.auth.FirebaseAuth

class CategoryActivity : AppCompatActivity(){

    private lateinit var mAuth: FirebaseAuth
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val preferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userName = preferences.getString("userName", "Usuario")
        findViewById<TextView>(R.id.NombreTexto).text = "Hola $userName"

        mAuth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener { logoutUser() }

        findViewById<ImageView>(R.id.button_fiction).setOnClickListener { returnCategory("Ficción") }
        findViewById<ImageView>(R.id.button_non_fiction).setOnClickListener { returnCategory("No Ficción") }
        findViewById<ImageView>(R.id.button_history).setOnClickListener { returnCategory("Historia") }
        findViewById<ImageView>(R.id.button_mystery).setOnClickListener { returnCategory("Misterio") }
        findViewById<ImageView>(R.id.button_science).setOnClickListener { returnCategory("Ciencia") }
    }

    private fun returnCategory(category: String) {
        val resultIntent = Intent().putExtra("selected_category", category)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun logoutUser() {
        mAuth.signOut()
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        preferences.edit().apply {
            putBoolean("isLoggedIn", false)
            remove("userEmail")
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}