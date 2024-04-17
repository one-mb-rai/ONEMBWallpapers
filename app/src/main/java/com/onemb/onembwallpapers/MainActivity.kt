package com.onemb.onembwallpapers

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        installSplashScreen().setKeepOnScreenCondition(condition = { keepSplashScreen })
        setContent {
            ONEMBWallpapersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val hasEffectRun = rememberSaveable { mutableStateOf(false) }

                        val preferences = viewModel.getSharedPreferences(app)
                        val wallpapersState = viewModel.wallpapers.observeAsState()
                        val isLoading = viewModel.isLoading.collectAsState(initial = false).value
                        val isOnboardingDone = viewModel.onboardingDone.collectAsState(initial = true)

                        viewModel.setOnboarding(preferences.getBoolean("onboardingDone", false))
                        LaunchedEffect(Unit) {
                            if (!hasEffectRun.value) {
                                viewModel.loadLocalJson(app)
                                hasEffectRun.value = true
                            }
                        }
                        val isCategoriesSelected: Boolean = viewModel.getSelectedCollection(
                            app,
                            app.getString(R.string.app_collection_key)
                        ).isNotEmpty()
                        wallpapersState.value.let {
                            if (it?.isNotEmpty() == true) {
                                LandingNavigation(viewModel, isCategoriesSelected)
                                keepSplashScreen = false
                            }
                        }

                        if (isLoading) {
                            Surface(
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxSize(),
                                tonalElevation = 16.dp,
                                shadowElevation = 16.dp,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceAround,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .width(200.dp),
                                        strokeWidth = 6.dp,
                                    )
                                }
                            }
                        }
                        if (!isOnboardingDone.value) {
                            OnboardingScreen(viewModel)
                        }

                    }
                }
            }
        }
    }

}
