package com.example.recipeapp2

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), RecipeAdapter.OnRecipeListener {

    private val mHandler = Handler()
    private lateinit var dialogSpinner: Spinner
    private lateinit var dialog: Dialog
    private val PERMISSION_CODE = 100
    private val IMAGE_CHOOSE_CODE = 200
    private lateinit var imageView: ImageView
    private var imageSelected = false
    private lateinit var mView: View
    private lateinit var adapter:RecipeAdapter
    private lateinit var recipesList:ArrayList<Recipe>
    private lateinit var filteredRecipe:ArrayList<Recipe>
    private var firstLaunched = true
    private lateinit var mSpinner:Spinner

    private val mContext = this

    companion object{
        lateinit var dbHandler: DBHandler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeUI()

        dbHandler = DBHandler(this, null, null, 1)
        viewRecipes()

    }

    private fun initializeUI(){
        mSpinner = findViewById(R.id.mainRecipeTypeSpinner)
        mSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem:String = parent!!.getItemAtPosition(position).toString()
                val rv:RecyclerView = findViewById(R.id.recipeRecycleView)
                if(selectedItem!="All"){
                    filteredRecipe = recipesList.filter { recipe -> recipe.recipeType == selectedItem } as ArrayList<Recipe>
                    adapter = RecipeAdapter(mContext, filteredRecipe as ArrayList<Recipe>, mContext)
                    rv.adapter = adapter

                } else {
                    adapter = RecipeAdapter(mContext, recipesList, mContext)
                    rv.adapter = adapter

                }
                rv.addItemDecoration(DividerItemDecoration(rv.context,DividerItemDecoration.VERTICAL))

            }

        }
        ArrayAdapter.createFromResource(this, R.array.recipe_type, android.R.layout.simple_spinner_item)
            .also {adapter->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                mSpinner.adapter = adapter
            }
        mHandler.postDelayed({
            mSpinner.setSelection(0)
        },100)
        val mFAB:ExtendedFloatingActionButton = findViewById(R.id.mainAddRecipeBtn)
        mFAB.setOnClickListener {
            displayAddRecipeLayout()
        }
    }

    private fun displayAddRecipeLayout(){
        imageSelected = false
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        mView = layoutInflater.inflate(R.layout.custom_add_recipe_layout, null)
        val addIngredientEditText:TextInputEditText = mView.findViewById(R.id.addIngredientEditText)
        dialogSpinner = mView.findViewById(R.id.addRecipeTypeSpinner) as Spinner
        getAlertSpinnerContent()
        dialogBuilder.setCancelable(false)
        dialogBuilder.setView(mView)
        dialog = dialogBuilder.create()
        dialog.show()
        imageView = mView.findViewById(R.id.addRecipeImageView)
    }

    private fun getAlertSpinnerContent(){
        var recipesType:Array<String> = resources.getStringArray(R.array.recipe_type)
        //remove first element
        val tempList:MutableList<String> = recipesType.toMutableList()
        tempList.remove("All")
        recipesType = tempList.toTypedArray()
        val adapter = ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, recipesType)
        dialogSpinner.adapter = adapter
        mHandler.postDelayed({
            dialogSpinner.setSelection(0)
        },100)
    }

    fun cancelDialog(view: View){
        dialog.cancel()
    }

    fun addNewRecipe(view: View){
        var error = false
        val recipeNameTextInputLayout:TextInputLayout = mView.findViewById(R.id.addRecipeNameTextLayout)
        val recipeNameEditText:TextInputEditText = mView.findViewById(R.id.addRecipeNameEditText)
        val recipeTypeSpinner:Spinner = mView.findViewById(R.id.addRecipeTypeSpinner)
        val recipeImageView:ImageView = mView.findViewById(R.id.addRecipeImageView)
        val recipeIngredientTextInputLayout:TextInputLayout = mView.findViewById(R.id.addRecipeIngredientTextLayout)
        val recipeIngredientText: TextInputEditText = mView.findViewById(R.id.addIngredientEditText)
        val recipeStepsTextInputLayout: TextInputLayout = mView.findViewById(R.id.addRecipeStepTextLayout)
        val recipeStepsText: TextInputEditText = mView.findViewById(R.id.addStepsEditText)
        if(recipeNameEditText.text.isNullOrBlank()){
            recipeNameTextInputLayout.error = "Recipe name cannot be empty!"
            error = true
        } else {
            recipeNameTextInputLayout.error = null
            error = false
        }
        if(recipeIngredientText.text.isNullOrBlank()){
            recipeIngredientTextInputLayout.error = "Ingredient cannot be empty!"
            error = true
        } else {
            recipeIngredientTextInputLayout.error = null
            error = false
        }
        if(recipeStepsText.text.isNullOrBlank()){
            recipeStepsTextInputLayout.error = "Step cannot be empty!"
            error = true
        } else{
            recipeStepsTextInputLayout.error = null
            error = false
        }

        if(!error){
            val recipe = Recipe()
            recipe.recipeName = recipeNameEditText.text.toString()
            recipe.recipeType = recipeTypeSpinner.selectedItem.toString()
            if(!imageSelected){
                recipe.recipeImageByte = null
            } else {
                recipe.recipeImageByte = convertImageToBLOB(recipeImageView)
            }
            recipe.recipeIngredient = recipeIngredientText.text.toString()
            recipe.recipeSteps = recipeStepsText.text.toString()
            dbHandler.addRecipe(this, recipe)
            dialog.cancel()
            viewRecipes()
            adapter.notifyDataSetChanged()
            //println("${recipe.recipeName}, ${recipe.recipeType}, ${recipe.recipeIngredient}, ${recipe.recipeSteps}")
        }

    }

    private fun convertImageToBLOB(imageView:ImageView):ByteArray{
        val drawable = imageView.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 ,outputStream)
        return outputStream.toByteArray()
    }

    fun selectImage(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                chooseImageFromGallery()
            }
        } else {
            chooseImageFromGallery()
        }
    }

    private fun chooseImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE-> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseImageFromGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_CHOOSE_CODE){
            //imageView.setImageURI(data?.data)
            Picasso.get().load(data?.data).rotate(90f).resize(500,500).onlyScaleDown().into(imageView)
            imageSelected = true
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun viewRecipes(){
        recipesList = dbHandler.getRecipes(this)
        adapter = RecipeAdapter(this, recipesList, this)
        val rv:RecyclerView = findViewById(R.id.recipeRecycleView)
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.adapter = adapter
    }

    override fun onRecipeClick(position: Int) {
        val intent = Intent(this, RecipeDetails::class.java)
        if(mSpinner.selectedItem.toString()=="All"){
            intent.putExtra("selected_recipe", recipesList[position])
        } else {
            intent.putExtra("selected_recipe", filteredRecipe[position])
        }
        startActivity(intent)
    }

    override fun onResume() {
        if(!firstLaunched){
            viewRecipes()
            mSpinner.setSelection(0)
            adapter.notifyDataSetChanged()
        } else {
            firstLaunched = false
        }
        super.onResume()
    }
}
