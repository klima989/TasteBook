package com.example.tastebook.data.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tastebook.data.Recipe
import com.example.tastebook.data.RecipeDao

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // For lists (ingredients, steps, tags)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}