package com.example.tastebook.data

import javax.inject.Inject

class RecipeRepository @Inject constructor
    (private val dao: RecipeDao) {

    suspend fun addRecipe(recipe: Recipe) = dao.insert(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = dao.delete(recipe)
    fun getAllRecipes() = dao.getALl()
    fun getByCategory(category: String) = dao.getByCategory(category)
    fun getByTag(tag: String) = dao.getByTag(tag)
}