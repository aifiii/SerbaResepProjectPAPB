package com.example.serbaresep

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.supabaseapp.supabase
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.serbaresep.adapter.RecipeAdapter
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream

@Serializable
data class Profile(
    val id: Int,
    val created_at: String,
    val full_name: String,
    val image: String?,
    val phone: String,
    val user_id: String
)


@Serializable
data class RecipeWrapper(
    val recipes: Recipe
)






class ProfileActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipeList: MutableList<Recipe> = mutableListOf()
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Memulai proses upload gambar
                uploadProfileImage(it)
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        // Initialize RecyclerView with LinearLayoutManager
        recyclerView = findViewById(R.id.recyclerViewRecipes) // Ganti 'recycler_view' dengan ID yang benar pada layout Anda

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchRecipes()
        recipeAdapter = RecipeAdapter(this, recipeList)
        recyclerView.adapter = recipeAdapter

        val logoutBtn = findViewById<Button>(R.id.btn_logout)
        val changeImgBtn = findViewById<Button>(R.id.btn_ganti_foto)
        val profileImg = findViewById<ImageView>(R.id.img_profile)
        val saveProfileBtn = findViewById<Button>(R.id.btn_save)


        saveProfileBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Ambil input dari EditText
                    val etFullName = findViewById<EditText>(R.id.et_full_name)
                    val etPhoneNumber = findViewById<EditText>(R.id.et_phone_number)

                    // Validasi input
                    val fullName = etFullName.text.toString().trim()
                    val phoneNumber = etPhoneNumber.text.toString().trim()

                    if (fullName.isEmpty() || phoneNumber.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Nama lengkap dan nomor telepon tidak boleh kosong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }

                    val userCredential = supabase.auth.currentUserOrNull()
                    if (userCredential?.id != null) {
                        val updateResponse = supabase.postgrest.from("profiles").update({
                            set("full_name", fullName)
                            set("phone", phoneNumber)
                        }) {
                            select(columns = Columns.ALL)
                            filter {
                                eq("user_id", userCredential.id)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Profil berhasil diperbarui.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "User tidak ditemukan. Silakan login kembali.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("ProfileActivity", "Error updating profile", e)
                }
            }
        }

        changeImgBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        logoutBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    supabase.auth.signOut()


                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@ProfileActivity, OnboardActivity::class.java)
                        startActivity(intent)
                        finish() // Mengakhiri aktivitas saat ini
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Gagal logout: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val userCredential = supabase.auth.currentUserOrNull()

        if (userCredential?.id != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val profileResponse = supabase
                        .postgrest.from("profiles").select(columns = Columns.ALL){
                            filter {
                                eq("user_id", userCredential.id)
                            }
                            single()
                        }

                    if (profileResponse != null) {
                        val profileData = Json.decodeFromString<Profile>(profileResponse.data)
                        val fullName = profileData.full_name
                        val phoneNumber = profileData.phone
                        val image = profileData.image
                        val email = userCredential.email


                        withContext(Dispatchers.Main) {
                            val etFullName = findViewById<EditText>(R.id.et_full_name)
                            val etPhoneNumber = findViewById<EditText>(R.id.et_phone_number)
                            val etEmail = findViewById<TextView>(R.id.et_email)

                            if (profileData.image != null) {
                                profileImg.load(image)
                            }

                            etFullName.text = Editable.Factory.getInstance().newEditable(fullName)
                            etEmail.text = Editable.Factory.getInstance().newEditable(email)
                            etPhoneNumber.text = Editable.Factory.getInstance().newEditable(phoneNumber)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Profil tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // redirect ke login
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun fetchRecipes() {
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Fetching data from Supabase
                val userId = supabase.auth.currentUserOrNull()?.id

                val recipeResult = supabase.postgrest
                    .from("favorite")
                    .select(columns = Columns.raw("recipes(id,nama_makanan,porsi,durasi_masak,cerita_asal_resep,bahan_utama,langkah_langkah,foto_makanan)")) {
                        filter {
                            if (userId != null) {
                                eq("user_id", userId)
                            }
                        }
                    }.decodeList<RecipeWrapper>()



                // Log the fetched data
                Log.d("ProfileActivity", "Fetched Favorites Recipes: ${recipeResult}")

                // Update the recipe list on the main thread
                withContext(Dispatchers.Main) {
                    recipeList.clear() // Mengosongkan daftar resep lama
                    recipeList.addAll(recipeResult.map { it.recipes }) // Menambahkan resep favorit ke dalam daftar
                    recipeAdapter.notifyDataSetChanged() // Memberi tahu adapter untuk memperbarui UI
                }

            } catch (e: Exception) {
                // Handle errors if any exception occurs
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("ProfileActivity", "Error fetching favorites: $e")
            }
        }
    }


    private fun uploadProfileImage(imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userCredential = supabase.auth.currentUserOrNull()
                if (userCredential?.id != null) {
                    // Ambil data profil pengguna saat ini
                    val profileResponse = supabase
                        .postgrest.from("profiles")
                        .select(columns = Columns.ALL) {
                            filter {
                                eq("user_id", userCredential.id)
                            }
                            single()
                        }

                    val profileData = Json.decodeFromString<Profile>(profileResponse.data)
                    val oldImageUrl = profileData.image // URL gambar lama

                    // Generate nama file untuk gambar baru
                    val fileName = UUID.randomUUID().toString() + ".jpg"
                    val bucket = supabase.storage.from("profileImg")

                    // Mengunggah gambar baru ke Supabase
                    val byteArray = getFileBytesFromUri(imageUri)
                    bucket.upload(fileName, byteArray)

                    // Dapatkan URL gambar baru yang diunggah
                    val newImageUrl = bucket.publicUrl(fileName)

                    // Perbarui URL gambar baru di profil pengguna
                    updateProfileImage(newImageUrl)

                    // Hapus gambar lama jika ada
                    oldImageUrl?.let {
                        val oldFileName = it.substringAfterLast("/") // Mendapatkan nama file dari URL
                        bucket.delete(oldFileName)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("ProfileActivity", "Error uploading profile image", e)
            }
        }
    }


    private fun getFileBytesFromUri(uri: Uri): ByteArray {
        val fileInputStream = contentResolver.openInputStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int

        while (fileInputStream?.read(buffer).also { length = it ?: -1 } != -1) {
            byteArrayOutputStream.write(buffer, 0, length)
        }

        fileInputStream?.close()
        return byteArrayOutputStream.toByteArray()
    }

    private fun updateProfileImage(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userCredential = supabase.auth.currentUserOrNull()
                if (userCredential?.id != null) {
                    // Memperbarui gambar di tabel profiles
                    val updateResponse = supabase
                        .postgrest.from("profiles")
                        .update(
                            {
                                set("image",imageUrl)
                            }
                        ){
                            filter {
                                eq("user_id", userCredential.id)
                            }
                        }
                    if (updateResponse.data != null) {
                        // Gambar berhasil di-update
                        withContext(Dispatchers.Main) {
                            val profileImg = findViewById<ImageView>(R.id.img_profile)
                            profileImg.load(imageUrl)
                            Toast.makeText(this@ProfileActivity, "Gambar profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Gagal memperbarui gambar: ${updateResponse}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.e("ProfileActivity", "Error updating profile image: $updateResponse")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("ProfileActivity", "Error updating profile image", e)
            }
        }
    }
}
