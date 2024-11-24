package com.example.serbaresep

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.serbaresep.adapter.RecipeAdapter
import com.example.supabaseapp.supabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipeList: MutableList<Recipe> = mutableListOf()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        recyclerView = findViewById(R.id.recyclerViewRecipes)

        // Initialize RecyclerView with LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)
        val ivProfile = findViewById<ImageView>(R.id.ivProfileImage)
        ivProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        val addBtn = findViewById<FloatingActionButton>(R.id.fabAddRecipe)
        addBtn.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

    fetchProfile()
        // Fetch recipe data from Supabase
        fetchRecipes()


        // Initialize adapter
        recipeAdapter = RecipeAdapter(this, recipeList)
        recyclerView.adapter = recipeAdapter
    }

    override fun onResume() {
        super.onResume()
        fetchProfile()
        // Fetch recipe data from Supabase
        fetchRecipes()

    }

    private fun fetchProfile(){
        val credential= supabase.auth.currentUserOrNull()
        val ivProfile = findViewById<ImageView>(R.id.ivProfileImage)
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

                    val tvFullName = findViewById<TextView>(R.id.tvProfileName)
                    ivProfile.load(profile.image)
                    tvFullName.text = "Selamat Datang! ${profile.full_name}"


                }catch(e:Exception){

                }
            }

        }

    }

    private fun fetchRecipes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetching data from Supabase
                val recipeResult = supabase.postgrest
                    .from("recipes")
                    .select(columns = Columns.list("id", "nama_makanan", "foto_makanan", "bahan_utama", "cerita_asal_resep", "durasi_masak", "langkah_langkah", "porsi"))
                    .decodeList<Recipe>()  // Decode query result into a list of Recipe

                // Update the recipe list on the main thread
                withContext(Dispatchers.Main) {
                    recipeList.clear()  // Clear the existing list
                    recipeList.addAll(recipeResult)  // Add the new data to the list
                    recipeAdapter.notifyDataSetChanged()  // Notify adapter of data change
                }

            } catch (e: Exception) {
                // Handle errors if any exception occurs
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
