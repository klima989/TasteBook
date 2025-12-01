package com.example.tastebook.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tastebook.R
import com.example.tastebook.data.Recipe
import com.example.tastebook.data.RecipeCategory
import com.example.tastebook.viewmodel.RecipeViewModel

@Composable
fun EditRecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    onRecipeSaved: (Recipe) -> Unit
) {
    val recipe by viewModel.previewRecipe.collectAsState()

    var title by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") } // editable as comma-separated string
    var steps by remember { mutableStateOf("") } // editable as comma-separated string
    var imageUrl by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") } // new editable field
    var subCategory by remember { mutableStateOf("") } // new editable field
    var selectedCategory by remember { mutableStateOf<RecipeCategory?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(recipe) {
        recipe?.let {
            title = it.title
            ingredients = it.ingredients?.joinToString(", ") ?: ""
            steps = it.steps?.joinToString(", ") ?: ""
            imageUrl = it.image ?: ""
            tags = it.tags.joinToString(", ")
            subCategory = it.subCategory
            selectedCategory = it.category
        }
    }

    Scaffold(
        topBar = {
            TopBar(stringResource(id = R.string.edit_recipe))
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    if (selectedCategory != null && title.isNotBlank()) {
                        val newOrUpdatedRecipe = recipe?.copy(
                            title = title,
                            ingredients = ingredients.split(",").map { it.trim() },
                            steps = steps.split(",").map { it.trim() },
                            image = imageUrl,
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            subCategory = subCategory,
                            category = selectedCategory!!
                        ) ?: Recipe(
                            title = title,
                            ingredients = ingredients.split(",").map { it.trim() },
                            steps = steps.split(",").map { it.trim() },
                            image = imageUrl,
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            subCategory = subCategory,
                            category = selectedCategory!!,
                            url = ""
                        )
                        viewModel.saveRecipe(newOrUpdatedRecipe)
                        onRecipeSaved(newOrUpdatedRecipe)
                    } else {
                        showError = true
                    }
                }) {
                    Text(stringResource(id = R.string.save_recipe))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Title
                Text(stringResource(id = R.string.title), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Image URL
                Text(stringResource(id = R.string.image_url), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category
                Text(stringResource(id = R.string.category), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedCategory == RecipeCategory.Sweet,
                        onCheckedChange = { if (it) selectedCategory = RecipeCategory.Sweet }
                    )
                    Text(
                        stringResource(id = R.string.tab_sweet),
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Checkbox(
                        checked = selectedCategory == RecipeCategory.Savory,
                        onCheckedChange = { if (it) selectedCategory = RecipeCategory.Savory }
                    )
                    Text(stringResource(id = R.string.tab_savory))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ingredients
                Text(stringResource(id = R.string.ingredients), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Steps
                Text(stringResource(id = R.string.steps), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tags
                Text(stringResource(id = R.string.tags), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = { Text(stringResource(id = R.string.hint_tags)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subcategory
                Text(stringResource(id = R.string.subcategory), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    placeholder = { Text(stringResource(id = R.string.hint_subcategory)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                errorMessage?.let {
                    Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        }

        if (showError) {
            ErrorDialog(
                stringResource(id = R.string.error),
                stringResource(id = R.string.category_error),
                { showError = false }
            )
        }
    }
}

@Preview
@Composable
fun PreviewEditRecipeScreen() {
    EditRecipeScreen(onRecipeSaved = {})
}
