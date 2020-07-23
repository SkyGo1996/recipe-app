package com.example.recipeapp2

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class RecipeDetails : AppCompatActivity() {
    lateinit var detailedRecipeImage:ImageView
    lateinit var detailedRecipeName:TextInputEditText
    lateinit var detailedRecipeTypeSpinner: Spinner
    lateinit var detailedRecipeIngredient:TextInputEditText
    lateinit var detailedRecipeSteps:TextInputEditText
    lateinit var detailedAppBar: MaterialToolbar
    var editMode = false
    var imageChange = false
    var editError = false

    private val IMAGE_CHOOSE_CODE = 500

    lateinit var recipe:Recipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        if(intent.hasExtra("selected_recipe")){
            recipe = intent.getParcelableExtra("selected_recipe")!!
        }

        initializeUI()
    }

    private fun initializeUI(){
        detailedRecipeImage = findViewById(R.id.details_recipe_image)
        detailedRecipeName = findViewById(R.id.details_recipe_name)
        detailedRecipeTypeSpinner = findViewById(R.id.details_recipe_type_spinner)
        detailedRecipeIngredient = findViewById(R.id.details_recipe_ingredient)
        detailedRecipeSteps = findViewById(R.id.details_recipe_steps)
        detailedAppBar = findViewById(R.id.details_app_bar)

        detailedRecipeTypeSpinner.isEnabled = false
        detailedRecipeImage.isEnabled = false
        detailedRecipeImage.setOnClickListener {
            loadImageFromGallery()
        }
        if(recipe.recipeImageByte==null){
            detailedRecipeImage.setImageResource(R.drawable.ic_image_black_24dp)
        } else{
            detailedRecipeImage.setImageBitmap(BitmapFactory.decodeByteArray(recipe.recipeImageByte,0,recipe.recipeImageByte?.size ?: 0))
        }

        detailedRecipeName.setText(recipe.recipeName)

        //populate spinner
        var recipesType:Array<String> = resources.getStringArray(R.array.recipe_type)
        //remove first element
        val tempList:MutableList<String> = recipesType.toMutableList()
        tempList.remove("All")
        recipesType = tempList.toTypedArray()
        val adapter = ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, recipesType)
        detailedRecipeTypeSpinner.adapter = adapter
        //set to recipe type value
        val spinnerPositionToBeSet = adapter.getPosition(recipe.recipeType)
        detailedRecipeTypeSpinner.setSelection(spinnerPositionToBeSet)

        detailedRecipeIngredient.setText(recipe.recipeIngredient)
        detailedRecipeSteps.setText(recipe.recipeSteps)

        detailedAppBar.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId){
                R.id.menu_edit->{
                    if(editMode){
                        editValidate()
                    } else {
                        editRecipe()
                    }
                    true
                }
                R.id.menu_delete->{
                    if(editMode){
                        editEnd()
                    } else {
                        deleteRecipe()
                    }
                    true
                }
                else ->false
            }
        }
    }

    private fun deleteRecipe(){
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Delete Recipe: ${recipe.recipeName}")
            .setMessage("Are you sure you want to delete ${recipe.recipeName}?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                if(MainActivity.dbHandler.deleteRecipe(recipe.recipeID)){
                    Toast.makeText(this, "Delete Successfully", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: cannot delete the selected recipe!", Toast.LENGTH_LONG).show()

                }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

            }).show()
    }

    private fun editRecipe(){
        editMode = true
        detailedRecipeImage.isEnabled = true
        detailedRecipeImage.alpha = 0.5f
        detailedRecipeName.isEnabled = true
        detailedRecipeTypeSpinner.isEnabled = true
        detailedRecipeIngredient.isEnabled = true
        detailedRecipeSteps.isEnabled = true
        detailedAppBar.menu.getItem(0).setIcon(R.drawable.ic_check_24px)
        detailedAppBar.menu.getItem(1).setIcon(R.drawable.ic_clear_24px)
        detailedRecipeName.requestFocus()

    }

    private fun editEnd(){
        editMode = false
        detailedRecipeImage.isEnabled = false
        detailedRecipeImage.alpha = 1.0f
        detailedRecipeName.isEnabled = false
        detailedRecipeTypeSpinner.isEnabled = false
        detailedRecipeIngredient.isEnabled = false
        detailedRecipeSteps.isEnabled = false
        detailedAppBar.menu.getItem(0).setIcon(R.drawable.ic_edit_white_24dp)
        detailedAppBar.menu.getItem(1).setIcon(R.drawable.ic_delete_white_24dp)
        imageChange = false
    }

    private fun editValidate(){
        if(detailedRecipeName.text.isNullOrBlank()){
            detailedRecipeName.error = "Name cannot be empty!"
            editError = true

        } else {
            detailedRecipeName.error = null
            editError = false
        }
        if(detailedRecipeIngredient.text.isNullOrBlank()){
            detailedRecipeIngredient.error = "Ingredient cannot be empty!"
            editError = true
        } else {
            detailedRecipeIngredient.error = null
            editError = false
        }
        if(detailedRecipeSteps.text.isNullOrBlank()){
            detailedRecipeSteps.error = "Step cannot be empty"
            editError = true
        } else {
            detailedRecipeSteps.error = null
            editError = false
        }

        if(!editError){
            recipe.recipeName = detailedRecipeName.text.toString()
            recipe.recipeType = detailedRecipeTypeSpinner.selectedItem.toString()
            if(imageChange){
                detailedRecipeImage.alpha = 1.0f
                recipe.recipeImageByte = convertImageToBLOB(detailedRecipeImage)
            }
            recipe.recipeIngredient = detailedRecipeIngredient.text.toString()
            recipe.recipeSteps = detailedRecipeSteps.text.toString()

            if(MainActivity.dbHandler.editRecipe(recipe)){
                Toast.makeText(this, "Successfully updated", Toast.LENGTH_LONG).show()
                editEnd()
            } else{
                Toast.makeText(this, "Fail update to db!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_CHOOSE_CODE){
            //detailedRecipeImage.setImageURI(data?.data)
            Picasso.get().load(data?.data).rotate(90f).resize(500,500).onlyScaleDown().into(detailedRecipeImage)
            imageChange = true
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun convertImageToBLOB(imageView:ImageView):ByteArray{
        val drawable = imageView.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50 ,outputStream)
        return outputStream.toByteArray()
    }
}
