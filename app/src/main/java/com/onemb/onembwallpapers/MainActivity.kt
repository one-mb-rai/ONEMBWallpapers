package com.onemb.onembwallpapers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onemb.onembwallpapers.composable.LandingNavigation
import com.onemb.onembwallpapers.ui.theme.ONEMBWallpapersTheme
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var keepSplashScreen = true
        installSplashScreen().setKeepOnScreenCondition(condition = { keepSplashScreen })
        setContent {
            ONEMBWallpapersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasEffectRun = rememberSaveable { mutableStateOf(false) }

                    val context = this
                    val viewModel: WallpaperViewModel = viewModel()
                    val wallpapersState = viewModel.wallpapers.observeAsState()
                    val isLoading = viewModel.isLoading.collectAsState(initial = false).value
                    LaunchedEffect(Unit) {
                        if (!hasEffectRun.value) {
                            Log.d("CALLED", "CALLED")
                            viewModel.loadLocalJson(context)
                            hasEffectRun.value = true
                        }
                    }
                    val isCategoriesSelected: Boolean = viewModel.getSelectedCollection(this, this.getString(R.string.app_collection_key)).isNotEmpty()
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
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth(0.7f),
                                    strokeWidth = 4.dp,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!checkPermission()) {
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests notification permission from the user.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            7562
        )
    }

    /**
     * Handles the result of the permission request.
     */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            7562 -> {

                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
