package com.example.tastebook.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tastebook.R
import com.example.tastebook.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun AddRecipeScreen(navController: NavController, viewModel: RecipeViewModel) {
    var showUrlInput by remember { mutableStateOf(false) }
    var recipeUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBar(stringResource(id = R.string.add_recipe))
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Add Recipe",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { showUrlInput = !showUrlInput },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Add from URL") }

                if (showUrlInput) {
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = recipeUrl,
                        onValueChange = { recipeUrl = it },
                        label = { Text("Recipe URL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { }
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                val recipe = viewModel.loadRecipeFromUrl(recipeUrl)
                                isLoading = false

                                if (recipe != null) {
                                    viewModel.setPreviewRecipe(recipe)
                                    navController.navigate("previewRecipe")
                                } else {
                                    errorMessage = "Failed to parse recipe. Please check the URL"
                                }

                            }
                        },
                        enabled = recipeUrl.isNotBlank() && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Parse Recipe")
                        }
                    }

                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { navController.navigate("editRecipe") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Manually")
                }
            }
        }
    )
}