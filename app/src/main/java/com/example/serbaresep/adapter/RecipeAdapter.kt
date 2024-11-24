package com.example.serbaresep.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbaresep.R
import com.example.serbaresep.Recipe
import com.example.serbaresep.RecipeDetailActivity

class RecipeAdapter(
    private val context: Context,
    private val recipeList: List<Recipe>
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.bind(recipe)
    }

    override fun getItemCount(): Int = recipeList.size

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeName: TextView = itemView.findViewById(R.id.recipeName)

        fun bind(recipe: Recipe) {
            recipeName.text = recipe.nama_makanan

            // Handle click to open detail activity
            itemView.setOnClickListener {
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("recipe_id", recipe.id)
                    putExtra("recipe_name", recipe.nama_makanan)
                    putExtra("recipe_image", recipe.foto_makanan)
                    putExtra("recipe_ingredients", ArrayList(recipe.bahan_utama))
                    putExtra("recipe_story", recipe.cerita_asal_resep)
                    putExtra("recipe_duration", recipe.durasi_masak)
                    putExtra("recipe_steps", ArrayList(recipe.langkah_langkah))
                    putExtra("recipe_servings", recipe.porsi)
                }
                context.startActivity(intent)
            }
        }
    }
}