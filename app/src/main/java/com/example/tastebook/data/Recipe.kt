package com.example.tastebook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class RecipeCategory {
    Sweet,
    Savory
}

@Serializable
@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val image: String?,
    val ingredients: List<String>?,
    val steps: List<String>?,
    val url: String,
    val category: RecipeCategory,
    val subCategory: String,
    val tags: List<String> = emptyList()
)
