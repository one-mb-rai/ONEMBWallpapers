package com.onemb.onembwallpapers

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.onemb.onembwallpapers.composable.LandingNavigation
import com.onemb.onembwallpapers.composable.onboarding.OnboardingScreen
import com.onemb.onembwallpapers.ui.theme.ONEMBWallpapersTheme
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app: ONEMBApplication = this.applicationContext as ONEMBApplication
        val viewModel: WallpaperViewModel = app.wallpaperViewModel
        var keepSplashScreen = true
        val preferences = viewModel.getSharedPreferences(app)
        viewModel.setOnboarding(preferences.getBoolean("onboardingDone", false))

        installSplashScreen().setKeepOnScreenCondition(condition = { keepSplashScreen })
        setContent {
            ONEMBWallpapersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLoading = viewModel.isLoading.collectAsState(initial = false).value
                    val isOnboardingDone = viewModel.onboardingDone.collectAsState(initial = true).value
                    val isCategoriesSelected: Boolean = viewModel.getSelectedCollection(
                        app,
                        app.getString(R.string.app_collection_key)
                    ).isNotEmpty()

                    LaunchedEffect(viewModel) {
                        viewModel.loadLocalJson(app)
                    }

                    val wallpapers = viewModel.wallpapers.observeAsState(initial = null)

                    if (wallpapers.value?.isNotEmpty() == true) {
                        LandingNavigation(viewModel, isCategoriesSelected)
                        keepSplashScreen = false
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth(0.5f),
                                strokeWidth = 6.dp,
                            )
                        }
                    }

                    if (!isOnboardingDone) {
                        OnboardingScreen(viewModel)
                    }
                }
            }
        }
    }
}