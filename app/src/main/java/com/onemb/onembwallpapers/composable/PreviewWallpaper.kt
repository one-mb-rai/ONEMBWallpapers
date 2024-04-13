package com.onemb.onembwallpapers.composable

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@SuppressLint("RestrictedApi", "StateFlowValueCalledInComposition")
@Composable
fun WallpaperPreview(
    bitmap: Bitmap,
    viewModel: WallpaperViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val hasPreViewPopped = remember { mutableStateOf(false) }

    LaunchedEffect (null){
        Log.d("PreviewPage", navController.currentBackStack.value.toString())
        viewModel.wallpaperSet(false)
        viewModel.setLoading(false)
        viewModel.setNavigatedPreview(false)

    }

    if(viewModel.wallpaperSet.collectAsState(initial = false).value && !hasPreViewPopped.value) {
        hasPreViewPopped.value = true
        navController.popBackStack()
    }

    Box {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val state = rememberScrollState()

            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(state)
            ) {

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentScale = ContentScale.None,
                    contentDescription = null
                )
                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            if(viewModel.isForegroundServiceRunning(context)) {
                                val serviceIntent = Intent(
                                    context,
                                    WallpaperChangeForegroundService::class.java
                                )
                                context.stopService(serviceIntent)
                            }
                            viewModel.setWallpaper(
                                viewModel.getWallpaperBitmap()!!,
                                context
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = "Set Wallpaper")
                }
            }
        }
    }
}