package com.onemb.onembwallpapers.composable

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.viewmodels.WallpaperSetListener
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@Composable
fun WallpaperPreview(
    bitmap: Bitmap,
    viewModel: WallpaperViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current


    LaunchedEffect (null){
        viewModel.setLoading(false)
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
                        onNavigateBack()
                        viewModel.setLoading(true)
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