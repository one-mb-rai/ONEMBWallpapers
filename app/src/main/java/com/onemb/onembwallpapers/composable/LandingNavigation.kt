package com.onemb.onembwallpapers.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel

@Composable
fun LandingNavigation() {
    val navController = rememberNavController()
    val viewModel: WallpaperViewModel = viewModel()
    val context = LocalContext.current
    viewModel.loadLocalJson(context)

    val isCategoriesSelected = viewModel.getSelectedCollection(context, context.getString(R.string.app_collection_key)).isNotEmpty()

    NavHost(navController = navController, startDestination = if(isCategoriesSelected) "Home" else "Categories") {
        composable("Categories") { WallpaperCategory(navController, viewModel)}
        composable("Home") { WallpaperApp(navController, viewModel) }
        composable("Preview") {
            viewModel.getWallpaperBitmap()
                ?.let { it1 ->
                    WallpaperPreview(it1, viewModel, navController)
                }
        }
    }
}