package com.example.serbaresep

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.supabaseapp.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val nameEditText = findViewById<EditText>(R.id.et_name_register)
        val phoneEditText = findViewById<EditText>(R.id.et_phone_register)
        val emailEditText = findViewById<EditText>(R.id.et_email_register)
        val passwordEditText = findViewById<EditText>(R.id.et_password_register)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password_register)
        val registerButton = findViewById<Button>(R.id.btn_register)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val emailValue = emailEditText.text.toString().trim()
            val passwordValue = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty() && emailValue.isNotEmpty() && passwordValue.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (passwordValue == confirmPassword) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Sign up user
                            val result = supabase.auth.signUpWith(Email) {
                                email = emailValue
                                password = passwordValue
                            }

                            val user = supabase.auth.retrieveUserForCurrentSession(updateSession = true)


                            if (user.id != null) {
                                val profile = buildJsonObject {
                                    put("user_id", user.id)
                                    put("full_name", name)
                                    put("phone", phone)
                                }
                                try {
                                    val profileResult = supabase.from("profiles").insert(
                                        profile
                                    )

                                }catch (e:Exception){
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    Log.e("RegisterActivity", "Error saat membuat profile", e)

                                }



                            } else {
                                throw Exception("Sign-up failed, user ID not returned")
                            }

                            // Show success message
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@RegisterActivity, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            Log.e("RegisterActivity", "Error saat registrasi", e)
                        }
                    }
                } else {
                    Toast.makeText(this, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
