package com.example.recipeapp2

import android.os.Parcel
import android.os.Parcelable
import java.sql.Blob

class Recipe() : Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeInt(recipeID)
        dest.writeString(recipeName)
        dest.writeString(recipeType)
        dest.writeByteArray(recipeImageByte)
        dest.writeString(recipeIngredient)
        dest.writeString(recipeSteps)
    }

    override fun describeContents(): Int {
        return 0
    }

    var recipeID:Int = 0
    var recipeName:String = ""
    var recipeType:String = ""
    var recipeImageByte: ByteArray? = null
    var recipeIngredient:String = ""
    var recipeSteps:String = ""

    constructor(parcel: Parcel) : this() {
        recipeID = parcel.readInt()
        recipeName = parcel.readString()!!
        recipeType = parcel.readString()!!
        recipeImageByte = parcel.createByteArray()
        recipeIngredient = parcel.readString()!!
        recipeSteps = parcel.readString()!!
    }

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe {
            return Recipe(parcel)
        }

        override fun newArray(size: Int): Array<Recipe?> {
            return arrayOfNulls(size)
        }
    }

}