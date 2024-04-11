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
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel

@Composable
fun LandingNavigation() {
    val navController = rememberNavController()
    val viewModel: WallpaperViewModel = viewModel()
    val context = LocalContext.current
    val bitmapLoaded = viewModel.wallpapersBitmapLoaded.observeAsState()
    viewModel.loadLocalJson(context)
    NavHost(navController = navController, startDestination = "Home") {
        composable("Home") { WallpaperApp(navController, viewModel) }
        composable("Preview") {
            if(bitmapLoaded.value == false) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(200.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                viewModel.getWallpaperBitmap()
                    ?.let { it1 ->
                        WallpaperPreview(it1, viewModel, navController)
                    }
            }
        }
    }
}