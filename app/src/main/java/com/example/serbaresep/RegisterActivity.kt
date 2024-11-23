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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.log

class RegisterActivity : AppCompatActivity() {

    private val auth = supabase.auth // Instance Supabase Auth

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
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val emailValue = emailEditText.text.toString()
            val passwordValue = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && emailValue.isNotEmpty() && passwordValue.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (passwordValue == confirmPassword) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {

                            val metadata: JsonObject = buildJsonObject {
                                put("name", name)
                                put("phone", phone)
                            }


                            val result = supabase.auth.signUpWith(Email) {
                                email = emailValue
                                password = passwordValue
//                                data = metadata
                            }


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
