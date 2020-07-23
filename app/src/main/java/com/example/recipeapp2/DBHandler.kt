package com.example.recipeapp2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

class DBHandler(context: Context, name:String?, factory: SQLiteDatabase.CursorFactory?, version:Int) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION){
    companion object{
        private val DATABASE_NAME = "Recipe.db"
        private val DATABASE_VERSION = 1

        val RECIPE_TABLE_NAME = "Recipe"
        val COLUMN_RECIPE_ID = "Recipe_id"
        val COLUMN_RECIPE_NAME = "Recipe_name"
        val COLUMN_RECIPE_TYPE = "Recipe_type"
        val COLUMN_RECIPE_IMAGE = "Recipe_image"
        val COLUMN_RECIPE_INGREDIENT = "Recipe_ingredient"
        val COLUMN_RECIPE_STEPS = "Recipe_steps"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_RECIPE_TABLE = ("CREATE TABLE $RECIPE_TABLE_NAME (" +
                "$COLUMN_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_RECIPE_NAME TEXT,"+
                "$COLUMN_RECIPE_TYPE TEXT,"+
                "$COLUMN_RECIPE_IMAGE BLOB,"+
                "$COLUMN_RECIPE_INGREDIENT TEXT,"+
                "$COLUMN_RECIPE_STEPS TEXT)")
        db?.execSQL(CREATE_RECIPE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun getRecipes(context: Context): ArrayList<Recipe>{
        val qry = "SELECT * FROM $RECIPE_TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(qry, null)
        val recipes = ArrayList<Recipe>()

        if(cursor.count == 0){
            Toast.makeText(context, "No Record Found", Toast.LENGTH_LONG).show()
        } else {
            while(cursor.moveToNext()){
                val recipe = Recipe()
                recipe.recipeID = cursor.getInt(cursor.getColumnIndex(COLUMN_RECIPE_ID))
                recipe.recipeName = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_NAME))
                recipe.recipeType = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_TYPE))
                recipe.recipeImageByte = cursor.getBlob(cursor.getColumnIndex(COLUMN_RECIPE_IMAGE))
                recipe.recipeIngredient = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_INGREDIENT))
                recipe.recipeSteps = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_STEPS))
                recipes.add(recipe)
            }
            Toast.makeText(context, "Load completed, total of ${cursor.count} loaded", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
        db.close()
        return recipes
    }

    fun addRecipe(context: Context, recipe:Recipe){
        val values = ContentValues()
        values.put(COLUMN_RECIPE_NAME, recipe.recipeName)
        values.put(COLUMN_RECIPE_TYPE, recipe.recipeType)
        values.put(COLUMN_RECIPE_IMAGE, recipe.recipeImageByte)
        values.put(COLUMN_RECIPE_INGREDIENT, recipe.recipeIngredient)
        values.put(COLUMN_RECIPE_STEPS, recipe.recipeSteps)
        val db = this.writableDatabase
        try{
            db.insert(RECIPE_TABLE_NAME, null, values)
            Toast.makeText(context, "Recipe successfully added", Toast.LENGTH_LONG).show()
        } catch(e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
        db.close()
    }

    fun deleteRecipe(recipeID:Int):Boolean{
        val qry = "DELETE FROM $RECIPE_TABLE_NAME WHERE $COLUMN_RECIPE_ID = $recipeID"
        val db = this.writableDatabase
        var result:Boolean = false
        try{
            val cursor = db.execSQL(qry)
            result = true
        } catch(e: Exception){
            Log.e("TAG", "db deletion error with $e")
        }
        db.close()
        return result
    }

    fun editRecipe(recipe:Recipe): Boolean{
        var result = false
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_RECIPE_NAME, recipe.recipeName)
        contentValues.put(COLUMN_RECIPE_TYPE, recipe.recipeType)
        contentValues.put(COLUMN_RECIPE_IMAGE, recipe.recipeImageByte)
        contentValues.put(COLUMN_RECIPE_INGREDIENT, recipe.recipeIngredient)
        contentValues.put(COLUMN_RECIPE_STEPS, recipe.recipeSteps)
        try{
            db.update(RECIPE_TABLE_NAME, contentValues, "$COLUMN_RECIPE_ID = ?", arrayOf(recipe.recipeID.toString()))
            result = true
        } catch (e: Exception){
            Log.e("TAG", "db update error with $e")
            result = false
        }
        db.close()
        return result
    }

}