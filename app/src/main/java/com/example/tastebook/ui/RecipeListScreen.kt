package com.example.tastebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tastebook.data.Recipe
import com.example.tastebook.ui.NavRoutes.DETAILS_RECIPE
import com.example.tastebook.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    recipes: List<Recipe>,
    navController: NavController,
    onDeleteRecipe: (Recipe) -> Unit
) {
    LazyColumn {
        items(recipes.size) { recipe ->
            RecipeListItem(
                viewModel,
                recipes[recipe],
                navController,
                onDeleteClick = { onDeleteRecipe(recipes[recipe]) })
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipeListItem(
    viewModel: RecipeViewModel,
    recipe: Recipe,
    navController: NavController,
    onDeleteClick: (Recipe) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    )

    // When user finishes the swipe
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            // Show confirmation dialog
            showDialog = true
        }
    }

    // Confirmation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                scope.launch { dismissState.reset() }
            },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete \"${recipe.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(recipe)
                        showDialog = false
                        scope.launch { dismissState.reset() }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        scope.launch { dismissState.reset() }
                    }
                ) { Text("Cancel") }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Show background only when not in Settled state
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        // Set selected recipe in ViewModel and navigate
                        viewModel.selectRecipe(recipe)
                        navController.navigate(DETAILS_RECIPE)
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!recipe.image.isNullOrEmpty()) {
                        AsyncImage(
                            model = recipe.image,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp)
                        )
                    }
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    )
}