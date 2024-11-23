package com.example.serbaresep

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbaresep.MainActivity
import com.example.serbaresep.R
import com.example.supabaseapp.supabase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private val auth = supabase.auth // Instance Supabase Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val emailEditText = findViewById<EditText>(R.id.et_email_login)
        val passwordEditText = findViewById<EditText>(R.id.et_password_login)
        val loginButton = findViewById<Button>(R.id.btn_login)

        loginButton.setOnClickListener {
            val emailValue = emailEditText.text.toString()
            val passwordValue = passwordEditText.text.toString()

            if (emailValue.isNotEmpty() && passwordValue.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {

                        auth.signInWith(Email){
                            email = emailValue
                            password = passwordValue
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
