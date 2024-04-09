package com.onemb.onembwallpapers.composable

import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
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
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@Composable
fun WallpaperPreview(
    bitmap: Bitmap,
    viewModel: WallpaperViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var croppedBitmap: Bitmap? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val state = rememberScrollState()

        Box(
            Modifier
                .fillMaxSize()
                .verticalScroll(state)
        ) {
            CropImage(
                bitmap = bitmap,
                onCropCompleted = { croppedBitmap = it },
            )
            Button(
                onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.setWallpaper(viewModel.getWallpaperBitmap()!!, context)
                        }
                        viewModel.setWallpapersBitmapLoaded(false)
                        navController.popBackStack()
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
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CropImage(
    bitmap: Bitmap,
    onCropCompleted: (Bitmap) -> Unit
) {
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffset by remember { mutableStateOf(IntOffset.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current.density
    val context = LocalContext.current
    Box(
        Modifier.fillMaxSize()
            .pointerInteropFilter { event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    // Calculate the crop region based on the visible portion of the image
                    val left = (-imageOffset.x / imageScale).coerceAtLeast(0f)
                    val top = (-imageOffset.y / imageScale).coerceAtLeast(0f)
                    val right = ((-imageOffset.x + (getScreenWidth(context) / density)) / imageScale).coerceAtMost(imageSize.width)
                    val bottom = ((-imageOffset.y + (getScreenHeight(context) / density)) / imageScale).coerceAtMost(imageSize.height)

                    // Create a cropped bitmap
                    val croppedBitmap = Bitmap.createBitmap(bitmap, left.toInt(), top.toInt(), (right - left).toInt(), (bottom - top).toInt())

                    // Invoke the callback with the cropped bitmap
                    onCropCompleted(croppedBitmap)
                }
                true
            }
            .onGloballyPositioned { coordinates ->
                imageSize = coordinates.size.toSize()
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = imageScale,
                    scaleY = imageScale,
                    translationX = imageOffset.x.toFloat(),
                    translationY = imageOffset.y.toFloat()
                )
        )
    }
}
