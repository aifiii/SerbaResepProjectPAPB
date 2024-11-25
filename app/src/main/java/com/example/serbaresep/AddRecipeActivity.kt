package com.example.serbaresep
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.provider.MediaStore
import android.net.Uri
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.example.serbaresep.R
import com.example.supabaseapp.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream

@Serializable
data class Recipe(
    val id: Int,
    val nama_makanan: String,
    val porsi: Int,
    val durasi_masak: String,
    val cerita_asal_resep: String,
    val bahan_utama: List<String>,
    val langkah_langkah: List<String>,
    val foto_makanan: String? = null
)

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var etNamaMakanan: EditText
    private lateinit var etPorsi: EditText
    private lateinit var etDurasiMasak: EditText
    private lateinit var etCerita: EditText
    private lateinit var etBahanUtama: EditText
    private lateinit var etLangkahLangkah: EditText
    private lateinit var ivFoodImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSimpan: Button

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_recipe)

        // Initialize views
        etNamaMakanan = findViewById(R.id.etNamaMakanan)
        etPorsi = findViewById(R.id.etPorsi)
        etDurasiMasak = findViewById(R.id.etDurasiMasak)
        etCerita = findViewById(R.id.etCerita)
        etBahanUtama = findViewById(R.id.etBahanUtama)
        etLangkahLangkah = findViewById(R.id.etLangkahLangkah)
        ivFoodImage = findViewById(R.id.ivFoodImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Select image from gallery
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(intent)
        }

        // Save recipe
        btnSimpan.setOnClickListener {
            val namaMakanan = etNamaMakanan.text.toString()
            val porsiText = etPorsi.text.toString()
            val durasiMasak = etDurasiMasak.text.toString()
            val cerita = etCerita.text.toString()
            val bahanUtama = etBahanUtama.text.toString().split(",").map { it.trim() }
            val langkahLangkah = etLangkahLangkah.text.toString().split(",").map { it.trim() }

            // Validate fields
            if (namaMakanan.isEmpty() || porsiText.isEmpty() || durasiMasak.isEmpty() || cerita.isEmpty() ||
                bahanUtama.isEmpty() || langkahLangkah.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val porsi = porsiText.toIntOrNull() ?: 0

            // Upload image to Supabase Storage and get the URL
            selectedImageUri?.let { uri ->
                uploadImageToSupabase(uri) { imageUrl ->
                    // Create a Recipe object with image URL

                    val bahanUtamaJsonArray = JsonArray(bahanUtama.map { JsonPrimitive(it) })
                    val langkahLangkahJsonArray = JsonArray(langkahLangkah.map { JsonPrimitive(it) })
                    val credential = supabase.auth.currentUserOrNull()
                    val recipe = buildJsonObject {
                        put("nama_makanan", namaMakanan)
                        put("porsi",porsi)
                        put("durasi_masak",durasiMasak)
                        put("cerita_asal_resep",cerita)
                        put("bahan_utama",bahanUtamaJsonArray)
                        put("langkah_langkah",langkahLangkahJsonArray)
                        put("foto_makanan",imageUrl)
                        put("user_id",credential?.id)

                    }
//
                    // Add the recipe to the database
                    addRecipe(recipe)
                }
            }
        }
    }
    private fun fetchProfile(){
        val credential= supabase.auth.currentUserOrNull()
        val tvFullName = findViewById<TextView>(R.id.tvFull_Name)
        if (credential == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else
        {
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    val  profile = supabase.postgrest.from("profiles").select(columns = Columns.ALL){filter {
                        eq("user_id", credential.id)
                    }
                    }.decodeSingle<Profile>()



                    tvFullName.text = "Hello, ${profile.full_name}"


                }catch(e:Exception){

                }
            }

        }

    }

    // Register for activity result (selecting image from gallery)
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
            ivFoodImage.setImageURI(selectedImageUri)
        } else {
            Toast.makeText(this, "Gambar tidak dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    // Upload image to Supabase Storage
    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        // Get a reference to Supabase Storage
        val storage = supabase.storage.from("recipe-images") // Replace with your bucket name

        // Get the file name from the URI
        val fileName = "recipe_${System.currentTimeMillis()}.jpg" // Or use any unique naming strategy

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Open the file input stream
                val byteArray = getFileBytesFromUri(uri)

                // Start uploading the image
                val uploadResponse = storage.upload(fileName, byteArray)

                // Check if the upload is successful
                if (uploadResponse != null) {
                    // Get the public URL of the uploaded image
                    val imageUrl = storage.publicUrl(fileName)

                    withContext(Dispatchers.Main) {
                        onSuccess(imageUrl) // Pass the image URL back to the calling function
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddRecipeActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddRecipeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Add recipe to Supabase database
    private fun addRecipe(recipe: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postgrest = supabase.postgrest.from("recipes") // Replace with your table name
                val response = postgrest.insert(recipe)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@AddRecipeActivity, "Recipe saved successfully", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this@AddRecipeActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddRecipeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
}
