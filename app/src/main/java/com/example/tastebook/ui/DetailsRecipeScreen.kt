package com.example.tastebook.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastebook.R
import com.example.tastebook.viewmodel.RecipeViewModel

@Composable
fun DetailsRecipeScreen(
    viewModel: RecipeViewModel,
    onEditClick: () -> Unit
) {
    val recipe by viewModel.selectedRecipe.collectAsState()

    Scaffold(
        topBar = {
            TopBar(stringResource(id = R.string.recipe_details))
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.setPreviewRecipe(recipe)
                    onEditClick()
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Edit Recipe")
            }
        }
    ) { innerPadding ->
        if (recipe != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = recipe!!.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Image if available
                recipe!!.image?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = recipe!!.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp)
                    )
                }

                // Ingredients
                recipe!!.ingredients?.takeIf { it.isNotEmpty() }?.let { ingredients ->
                    Text(
                        text = "Ingredients",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    ingredients.forEach { ingredient ->
                        Text(
                            text = "- $ingredient",
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 32.dp, bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Steps
                recipe!!.steps?.takeIf { it.isNotEmpty() }?.let { steps ->
                    Text(
                        text = "Steps",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    steps.forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. $step",
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 32.dp, bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Space so button isn’t hidden
            }
        }
    }
}
