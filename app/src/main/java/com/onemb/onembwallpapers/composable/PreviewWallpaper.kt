package com.onemb.onembwallpapers.composable

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RestrictedApi", "StateFlowValueCalledInComposition")
@Composable
fun WallpaperPreview(
    bitmap: Bitmap,
    viewModel: WallpaperViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val hasPreViewPopped = remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect (null){
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

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    Column {
                        TextButton(
                            onClick = {
                                callSetWallpaperFunction(viewModel, context, "home")
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }) {
                            Icon(imageVector = Icons.Filled.Home, contentDescription = "")
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Home screen", color = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(
                            onClick = {
                                callSetWallpaperFunction(viewModel, context, "lockScreen")
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }) {
                            Icon(imageVector = Icons.Filled.Lock, contentDescription = "")
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Lock screen", color = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(
                            onClick = {
                                callSetWallpaperFunction(viewModel, context, "both")
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                        }) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = "")
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Home screen and lock screen", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }

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
                        showBottomSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = "Set Wallpaper", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    }
}

fun callSetWallpaperFunction(viewModel: WallpaperViewModel, context: Context, setOn: String) {
    viewModel.viewModelScope.launch {
        viewModel.setWallpaper(
            viewModel.getWallpaperBitmap()!!,
            context,
            setOn
        )
    }
}