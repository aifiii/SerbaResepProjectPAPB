package com.example.serbaresep

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.supabaseapp.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class favorite(
    val id: Int,
    val user_id: String,
    val recipe_id: Int,

    )
class RecipeDetailActivity : AppCompatActivity() {

    private var isFavorite = false  // Untuk menyimpan status favorit

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_detail)

        // Retrieve data from intent
        val recipeId = intent.getIntExtra("recipe_id", 0)
        val recipeName = intent.getStringExtra("recipe_name") ?: ""
        val recipeImage = intent.getStringExtra("recipe_image") ?: ""
        val recipeIngredients = intent.getStringArrayListExtra("recipe_ingredients") ?: arrayListOf()
        val recipeStory = intent.getStringExtra("recipe_story") ?: ""
        val recipeDuration = intent.getStringExtra("recipe_duration")
        val recipeSteps = intent.getStringArrayListExtra("recipe_steps") ?: arrayListOf()
        val recipeServings = intent.getIntExtra("recipe_servings", 0)

        // Bind data to UI elements
        val recipeImageView: ImageView = findViewById(R.id.recipeImageView)
        val recipeNameTextView: TextView = findViewById(R.id.recipeNameTextView)
        val recipeIngredientsTextView: TextView = findViewById(R.id.recipeIngredientsTextView)
        val recipeStoryTextView: TextView = findViewById(R.id.recipeStoryTextView)
        val recipeDurationTextView: TextView = findViewById(R.id.recipeDurationTextView)
        val recipeStepsTextView: TextView = findViewById(R.id.recipeStepsTextView)
        val recipeServingsTextView: TextView = findViewById(R.id.recipeServingsTextView)
        val favoriteToggle: ImageView = findViewById(R.id.favoriteToggle)

        // Set data to UI elements
        recipeImageView.load(recipeImage)
        recipeNameTextView.text = recipeName
        recipeIngredientsTextView.text = recipeIngredients.joinToString(separator = "\n") { "- $it" }
        recipeStoryTextView.text = recipeStory
        recipeDurationTextView.text = "Duration: $recipeDuration minutes"
        recipeStepsTextView.text = recipeSteps.joinToString(separator = "\n") { "- $it" }
        recipeServingsTextView.text = "Servings: $recipeServings"

        // Check if recipe is favorited
        CoroutineScope(Dispatchers.IO).launch {
            isFavorite = checkIfRecipeIsFavorited(recipeId)
            withContext(Dispatchers.Main) {
                updateFavoriteIcon(favoriteToggle, isFavorite)
            }
        }

        // Toggle favorite status
        favoriteToggle.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (isFavorite) {
                    unfavoriteRecipe(recipeId)
                    isFavorite = false
                } else {
                    favoriteRecipe(recipeId)
                    isFavorite = true
                }
                withContext(Dispatchers.Main) {
                    updateFavoriteIcon(favoriteToggle, isFavorite)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val favoriteToggle: ImageView = findViewById(R.id.favoriteToggle)
        val recipeId = intent.getIntExtra("recipe_id", 0)
        // Check if recipe is favorited
        CoroutineScope(Dispatchers.IO).launch {
            isFavorite = checkIfRecipeIsFavorited(recipeId)
            withContext(Dispatchers.Main) {
                updateFavoriteIcon(favoriteToggle, isFavorite)
            }
        }

        // Toggle favorite status
        favoriteToggle.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (isFavorite) {
                    unfavoriteRecipe(recipeId)
                    isFavorite = false
                } else {
                    favoriteRecipe(recipeId)
                    isFavorite = true
                }
                withContext(Dispatchers.Main) {
                    updateFavoriteIcon(favoriteToggle, isFavorite)
                }
            }
        }
    }


    private fun updateFavoriteIcon(favoriteToggle: ImageView, isFavorited: Boolean) {
        if (isFavorited) {
            favoriteToggle.setImageResource(R.drawable.ic_star)  // Bintang terisi
        } else {
            favoriteToggle.setImageResource(R.drawable.ic_star_empty)  // Bintang kosong
        }
    }

    private suspend fun checkIfRecipeIsFavorited(recipeId: Int): Boolean {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                val response = supabase.postgrest.from("favorite").select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("recipe_id", recipeId)
                    }
                }
                Log.e("Supabase", "checking favorite: ${response.decodeList<favorite>().isNotEmpty()}")

                return response.decodeList<favorite>().isNotEmpty()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("Supabase", "Error checking favorite: ${e.message}")
            false
        }
    }

    private suspend fun favoriteRecipe(recipeId: Int) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                val favorite = buildJsonObject {
                    put("user_id", userId)
                    put("recipe_id", recipeId)
                }
                supabase.postgrest.from("favorite").insert(favorite)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecipeDetailActivity, "Resep berhasil difavoritkan", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Supabase", "Error favoriting recipe: ${e.message}")
        }
    }

    private suspend fun unfavoriteRecipe(recipeId: Int) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                supabase.postgrest.from("favorite").delete {
                    filter {
                        eq("user_id", userId)
                        eq("recipe_id", recipeId)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecipeDetailActivity, "Resep berhasil dihapus dari favorit", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Supabase", "Error unfavoriting recipe: ${e.message}")
        }
    }
}
