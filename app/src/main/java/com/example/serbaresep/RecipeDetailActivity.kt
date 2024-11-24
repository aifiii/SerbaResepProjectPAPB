package com.example.serbaresep

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load


class RecipeDetailActivity : AppCompatActivity() {

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
        val recipeDuration = intent.getIntExtra("recipe_duration", 0)
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

        recipeImageView.load(recipeImage)




        // Set data to UI elements
        recipeNameTextView.text = recipeName
        recipeIngredientsTextView.text = recipeIngredients.joinToString(separator = "\n") { "- $it" }
        recipeStoryTextView.text = recipeStory
        recipeDurationTextView.text = "Duration: $recipeDuration minutes"
        recipeStepsTextView.text = recipeSteps.joinToString(separator = "\n") { "- $it" }
        recipeServingsTextView.text = "Servings: $recipeServings"
    }
}
