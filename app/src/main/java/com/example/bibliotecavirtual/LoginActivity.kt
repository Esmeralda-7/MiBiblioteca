package com.example.bibliotecavirtual

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var skipButton: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        if (preferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        skipButton = findViewById(R.id.skip_button)

        loginButton.setOnClickListener { loginUser() }
        registerButton.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        skipButton.setOnClickListener { startMainActivityWithoutLogin() }
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
                    preferences.edit().apply {
                        putBoolean("isLoggedIn", true)
                        putString("userEmail", email)
                        apply()
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMessage = try {
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "Credenciales incorrectas. Intente nuevamente."
                            is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas. Intente nuevamente."
                            else -> "Error al iniciar sesión: ${task.exception?.message}"
                        }
                    } catch (e: Exception) {
                        "Error al iniciar sesión: ${e.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivityWithoutLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}