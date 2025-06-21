package com.example.bibliotecavirtual

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var editNombre: EditText
    private lateinit var editCorreo: EditText
    private lateinit var editContraseña: EditText
    private lateinit var botonRegistrar: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        editNombre = findViewById(R.id.editnombre)
        editCorreo = findViewById(R.id.editcorreo)
        editContraseña = findViewById(R.id.editcontraseña)
        botonRegistrar = findViewById(R.id.botonregistrar)

        botonRegistrar.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val nombre = editNombre.text.toString().trim()
        val email = editCorreo.text.toString().trim()
        val password = editContraseña.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    val preferences = getSharedPreferences("user_data", MODE_PRIVATE)
                    preferences.edit().putString("userName", nombre).apply()
                    Log.i("RegisterActivity", "registerUser: ")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Log.e("RegisterActivity", "Error al registrarse", task.exception)
                    Toast.makeText(this, "Error al registrarse: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}