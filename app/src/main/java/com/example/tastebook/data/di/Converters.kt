package com.example.tastebook.data.di

import androidx.room.TypeConverter
import com.example.tastebook.data.RecipeCategory

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toStringList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split(",")

    @TypeConverter
    fun fromCategory(value: RecipeCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): RecipeCategory = RecipeCategory.valueOf(value)
}