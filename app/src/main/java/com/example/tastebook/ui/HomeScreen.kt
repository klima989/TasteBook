package com.example.tastebook.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tastebook.R
import com.example.tastebook.data.RecipeCategory
import com.example.tastebook.ui.NavRoutes.SETTINGS
import com.example.tastebook.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, viewModel: RecipeViewModel) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs = listOf(
        stringResource(id = R.string.tab_sweet),
        stringResource(id = R.string.tab_savory)
    )
    val scope = rememberCoroutineScope()

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    var showSearchBar by remember { mutableStateOf(false) }

    val sweetRecipes by viewModel.getRecipesByType(RecipeCategory.Sweet)
        .collectAsState(initial = emptyList())

    val savoryRecipes by viewModel.getRecipesByType(RecipeCategory.Savory)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                onSearchIconClicked = {
                    showSearchBar = !showSearchBar
                },
                onSettingsClick = { navController.navigate(SETTINGS) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addRecipe") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {

            // Search bar + filter
            if (showSearchBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* optional: hide keyboard */ })
                    )

                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            }

            // Optional filter dialog
            if (showFilterDialog) {
                ConfirmationDialog(
                    stringResource(id = R.string.search_filter),
                    stringResource(id = R.string.search_dialog_message)
                ) {
                    showFilterDialog = false
                }
            }

            SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                    )
                }
            }

            // Pager with filtered lists
            HorizontalPager(state = pagerState) { page ->
                val filteredRecipes = when (page) {
                    0 -> sweetRecipes
                    1 -> savoryRecipes
                    else -> emptyList()
                }.filter { recipe ->
                    searchQuery.isBlank() || recipe.title.contains(searchQuery, ignoreCase = true)
                            || recipe.subCategory.contains(searchQuery, ignoreCase = true)
                            || recipe.tags.any { it.contains(searchQuery, ignoreCase = true) }
                }

                RecipeListScreen(
                    viewModel,
                    recipes = filteredRecipes,
                    navController = navController,
                    onDeleteRecipe = { viewModel.deleteRecipe(it) }
                )
            }
        }
    }
}