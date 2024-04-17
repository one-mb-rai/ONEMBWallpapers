package com.onemb.onembwallpapers.composable

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel

@Composable
fun LandingNavigation(
    viewModel: WallpaperViewModel,
    isCategoriesSelected: Boolean
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = if(isCategoriesSelected) "Home" else "Categories") {
        composable("Categories") {
            WallpaperCategory(
                navController,
                viewModel
            )
        }
        composable("Home") {
            WallpaperApp(
                navController,
                viewModel
            )

        }
        composable("Preview") {
            WallpaperPreview(
                viewModel.getWallpaperBitmap()!!,
                viewModel,
                navController
            )
        }
    }
}

