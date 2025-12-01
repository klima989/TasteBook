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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tastebook.R
import com.example.tastebook.data.RecipeCategory
import com.example.tastebook.viewmodel.RecipeViewModel

@Composable
fun RecipeScreen(
    onSaveRecipe: () -> Unit,
    onEditRecipe: () -> Unit,
    viewModel: RecipeViewModel
) {
    val recipe by viewModel.previewRecipe.collectAsState()

    var title by remember { mutableStateOf<String?>(null) }
    var ingredients by remember { mutableStateOf<List<String>?>(null) }
    var steps by remember { mutableStateOf<List<String>?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<RecipeCategory?>(null) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (recipe != null) {
            title = recipe!!.title
            ingredients = recipe!!.ingredients
            steps = recipe!!.steps
            imageUrl = recipe!!.image
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    selectedCategory?.let { category ->
                        val updatedRecipe = viewModel.previewRecipe.value?.copy(category = category)
                        if (updatedRecipe != null) {
                            viewModel.saveRecipe(updatedRecipe)
                        }
                        onSaveRecipe()
                    } ?: run {
                        showError = true
                    }
                }) {
                    Text(text = "Save Recipe")
                }
                Button(onClick = onEditRecipe) {
                    Text(text = "Edit Recipe")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Loading…")
                }
            } else {
                if (errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: $errorMessage")
                    }
                } else {
                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        title?.let { t ->
                            Text(
                                text = t,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        imageUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Recipe image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(bottom = 12.dp)
                            )
                        }

                        Text("Category", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedCategory == RecipeCategory.Sweet,
                                onCheckedChange = {
                                    if (it) selectedCategory = RecipeCategory.Sweet
                                }
                            )
                            Text(
                                text = stringResource(id = R.string.tab_sweet),
                                modifier = Modifier.padding(end = 16.dp)
                            )

                            Checkbox(
                                checked = selectedCategory == RecipeCategory.Savory,
                                onCheckedChange = {
                                    if (it) selectedCategory = RecipeCategory.Savory
                                }
                            )
                            Text(text = stringResource(id = R.string.tab_savory))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        ingredients?.let { ing ->
                            Text(text = "Ingredients:", fontWeight = FontWeight.SemiBold)
                            ing.forEach { item ->
                                Text(text = "- $item")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        steps?.let { st ->
                            Text(text = "Steps:", fontWeight = FontWeight.SemiBold)
                            st.forEachIndexed { idx, s ->
                                Text(text = "${idx + 1}. $s")
                            }
                        }
                        Spacer(modifier = Modifier.height(80.dp)) // give space so content is not hidden behind bottom bar
                    }
                }
            }
        }

        if (showError) {
            ErrorDialog(
                stringResource(id = R.string.error),
                stringResource(R.string.category_error_saving)
            ) {
                showError = false
            }
        }
    }
}

@Preview
@Composable
fun PreviewRecipeDetailsScreen() {
    RecipeScreen({}, {}, hiltViewModel())
}