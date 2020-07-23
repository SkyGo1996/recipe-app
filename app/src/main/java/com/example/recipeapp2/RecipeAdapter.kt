package com.example.recipeapp2

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_recipes.view.*

class RecipeAdapter (mContext: Context, val recipes: ArrayList<Recipe>, var onRecipeListener: OnRecipeListener): RecyclerView.Adapter<RecipeAdapter.ViewHolder>(){

    class ViewHolder(itemView: View, val onRecipeListener: OnRecipeListener):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        init {
            itemView.setOnClickListener(this)
        }

        val recycleRecipeImageView = itemView.recycleImageView
        val recycleRecipeName = itemView.recycleRecipeNameTextView
        val recycleRecipeType = itemView.recycleRecipeTypeTextView

        override fun onClick(v: View?) {
            onRecipeListener.onRecipeClick(adapterPosition)
        }
    }

    interface OnRecipeListener{
        fun onRecipeClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.layout_recipes, parent, false)
        return ViewHolder(v, onRecipeListener)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: RecipeAdapter.ViewHolder, position: Int) {
        val recipe: Recipe = recipes[position]
        holder.recycleRecipeName.text = recipe.recipeName
        holder.recycleRecipeType.text = recipe.recipeType
        if(recipe.recipeImageByte==null){
            holder.recycleRecipeImageView.setImageResource(R.drawable.ic_image_black_24dp)
        } else{
            holder.recycleRecipeImageView.setImageBitmap(BitmapFactory.decodeByteArray(recipe.recipeImageByte,0,recipe.recipeImageByte?.size ?: 0))
        }
    }

}