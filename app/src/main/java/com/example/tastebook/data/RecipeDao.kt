package com.example.tastebook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    @Query("SELECT * FROM Recipe")
    fun getALl(): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe")
    suspend fun getAllOnce(): List<Recipe>

    @Query("SELECT * FROM Recipe WHERE category = :category")
    fun getByCategory(category: String): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE tags LIKE '%' || :tag || '%'")
    fun getByTag(tag: String): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE title = :title AND category = :category LIMIT 1")
    suspend fun getRecipeByTitle(title: String, category: RecipeCategory): Recipe?

    @Delete
    suspend fun delete(recipe: Recipe)
}
