package com.onemb.onembwallpapers.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel

@Composable
fun LandingNavigation(viewModel: WallpaperViewModel, isCategoriesSelected: Boolean) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = if(isCategoriesSelected) "Home" else "Categories") {
        composable("Categories") {
            WallpaperCategory(
                onNavigateToHome = { navController.navigate("Home") },
                viewModel
            )
        }
        composable("Home") {
            WallpaperApp(
                onNavigateToPreview = { navController.navigate("previewRoute") },
                onNavigateToCategories = { navController.navigate("Categories") },
                viewModel
            )

        }
        navigation(startDestination = "Preview", route = "previewRoute") {
            composable("Preview") {
                WallpaperPreview(
                    viewModel.getWallpaperBitmap()!!,
                    viewModel,
                    onNavigateBack = { navController.navigate("Home") { popUpTo("Home")} })
            }
        }
    }
}

