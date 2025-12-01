package com.example.tastebook.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tastebook.data.Recipe
import com.example.tastebook.data.RecipeCategory
import com.example.tastebook.data.RecipeRepository
import com.example.tastebook.parser.UrlParserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    private val _previewRecipe = MutableStateFlow<Recipe?>(null)
    val previewRecipe = _previewRecipe.asStateFlow()

    // Existing previewRecipe for parsing, etc.
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    fun selectRecipe(recipe: Recipe) {
        _selectedRecipe.value = recipe
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.addRecipe(recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    suspend fun loadRecipeFromUrl(url: String): Recipe? {
        return withContext(Dispatchers.IO) {
            try {
                UrlParserHelper.fetchAndParseRecipe(url)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun getRecipesByType(category: RecipeCategory): Flow<List<Recipe>> {
        return repository.getAllRecipes().map { list ->
            list.filter { it.category == category }
        }
    }

    fun setPreviewRecipe(recipe: Recipe?) {
        _previewRecipe.value = recipe
    }
}