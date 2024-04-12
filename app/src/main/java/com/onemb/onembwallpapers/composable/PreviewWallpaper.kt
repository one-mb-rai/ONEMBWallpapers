package com.onemb.onembwallpapers.composable

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.onemb.onembwallpapers.utils.ScreenUtils.getScreenHeight
import com.onemb.onembwallpapers.utils.ScreenUtils.getScreenWidth
import com.onemb.onembwallpapers.viewmodels.WallpaperSetListener
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun WallpaperPreview(
    bitmap: Bitmap,
    viewModel: WallpaperViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    val listener = object : WallpaperSetListener {
        override suspend fun onWallpaperSet() {
            withContext(Dispatchers.Main) {
                navController.navigate("Home")
            }
        }

        override fun onWallpaperSetError(error: Throwable) {
            navController.navigate("Home")
        }
    }
    val isLoading = viewModel.isLoading.collectAsState(initial = false).value


    LaunchedEffect (null){
        viewModel._isLoading.value = false
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
                        viewModel._isLoading.value = true
                        viewModel.setWallpapersBitmapLoaded(false)
                        viewModel.viewModelScope.launch {
                            viewModel.setWallpaper(
                                viewModel.getWallpaperBitmap()!!,
                                context,
                                listener
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

        TransparentLoaderVisibility(isLoading)
    }
}