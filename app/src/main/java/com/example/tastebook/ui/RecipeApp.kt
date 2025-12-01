package com.example.tastebook.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tastebook.ui.NavRoutes.ADD_RECIPE
import com.example.tastebook.ui.NavRoutes.DETAILS_RECIPE
import com.example.tastebook.ui.NavRoutes.EDIT_RECIPE
import com.example.tastebook.ui.NavRoutes.HOME
import com.example.tastebook.ui.NavRoutes.PREVIEW_RECIPE
import com.example.tastebook.ui.NavRoutes.SETTINGS
import com.example.tastebook.viewmodel.DriveViewModel
import com.example.tastebook.viewmodel.RecipeViewModel

@Composable
fun RecipeApp() {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = hiltViewModel()
    val driveViewModel: DriveViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable(HOME) { HomeScreen(navController, viewModel) }
        composable(ADD_RECIPE) { AddRecipeScreen(navController, viewModel) }
        composable(PREVIEW_RECIPE) {
            RecipeScreen(
                { navController.navigate(HOME) },
                { navController.navigate(EDIT_RECIPE) },
                viewModel
            )
        }
        composable(EDIT_RECIPE) { EditRecipeScreen(viewModel, { navController.navigate(HOME) }) }
        composable(DETAILS_RECIPE) {
            DetailsRecipeScreen(viewModel) {
                navController.navigate(
                    EDIT_RECIPE
                )
            }
        }
        composable(SETTINGS) { SettingsScreen(driveViewModel, navController) { } }
    }
}