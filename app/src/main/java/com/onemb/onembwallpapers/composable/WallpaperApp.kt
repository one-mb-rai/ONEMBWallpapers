package com.onemb.onembwallpapers.composable

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.receivers.ONEMBReceiver
import com.onemb.onembwallpapers.services.Wallpaper
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.viewmodels.BitmapSetListener
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperApp(
    navController: NavController,
    viewModel: WallpaperViewModel,
    checkPermission: Boolean,
    requestPermission: () -> Unit
) {
    val context = LocalContext.current
    val wallpapersState = viewModel.wallpapers.observeAsState()
    val combinedDataList = mutableListOf<Wallpaper>()
    val keys = viewModel.getSelectedCollection(context, context.getString(R.string.app_collection_key))
    val serviceRunning = viewModel.serviceRunning.collectAsState(initial = false)

    for (key in keys) {
        val index = wallpapersState.value?.indexOfFirst { wallpaperKey ->  wallpaperKey.wallpapers.containsKey(key)}!!
        val dataForCurrentKey = wallpapersState.value?.get(index)?.wallpapers?.get(key)
        if (dataForCurrentKey != null) {
            combinedDataList.addAll(dataForCurrentKey)
        }
    }
    val navigatedPreview = viewModel.navigatedPreview.collectAsState(initial = false).value

    DisposableEffect(viewModel.isForegroundServiceRunning(context)) {
        if(viewModel.isForegroundServiceRunning(context)) {
            viewModel.setServiceRunning(true)
        }
        onDispose {  }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Wallpapers",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.loadLocalJson(context)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Localized description",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate("Categories")
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Category icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Back to Category", color = MaterialTheme.colorScheme.primary)
            }
        }
    ) { innerPadding ->
        val listener = object : BitmapSetListener {
            override suspend fun onBitmapSet() {
                if(!navigatedPreview) {
                    viewModel.setNavigatedPreview(true)
                    navController.navigate("Preview")
                }
            }
            override fun onBitmapSetError(error: Throwable) {

            }
        }

        val openAlertDialog = remember { mutableStateOf(false) }
        when {
            openAlertDialog.value -> {
                PermissionDialog(
                    onDismissRequest = { openAlertDialog.value = false },
                    onConfirmation = {
                        openAlertDialog.value = false
                        requestPermission()
                    },
                    dialogTitle = "Foreground Service permission request",
                    dialogText = {
                        Column {
                            Text(text = "This app requires Notifications permission to run this service.")
                            Text(text = "A permanent notification will be visible in notification drawer.")
                            Text(text = "Do you want to continue")
                        }
                    },
                    icon = Icons.Default.Info
                )
            }
        }
        Box {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(combinedDataList.size) { index ->
                    if (index == 0) {
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .fillMaxSize()
                                .height(200.dp)
                                .clickable {
                                    if (checkPermission) {
                                        if (!serviceRunning.value) {
                                            val intent = Intent(context, ONEMBReceiver::class.java)
                                            intent.action = "START_LOADING"
                                            context.sendBroadcast(intent)
                                            viewModel.viewModelScope.launch {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Wallpaper change service starting",
                                                        1000 * 3
                                                    )
                                                    .show()
                                                val serviceIntent = Intent(
                                                    context,
                                                    WallpaperChangeForegroundService::class.java
                                                )
                                                context.startService(serviceIntent)
                                                viewModel.setServiceRunning(true)
                                            }
                                        } else {
                                            viewModel.viewModelScope.launch {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Wallpaper change service stopping",
                                                        1000 * 3
                                                    )
                                                    .show()
                                                val serviceIntent = Intent(
                                                    context,
                                                    WallpaperChangeForegroundService::class.java
                                                )
                                                context.stopService(serviceIntent)
                                                viewModel.setServiceRunning(false)
                                            }
                                        }
                                    } else {
                                        openAlertDialog.value = true
                                    }
                                }
                        ) {
                            ElevatedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(IntrinsicSize.Max)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if(!serviceRunning.value) "Change every 30 minutes" else "Stop auto wallpaper change",
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .fillMaxSize()
                                .height(200.dp)
                                .clickable {
                                    viewModel.setLoading(true)
                                    combinedDataList[index].url.let {
                                        viewModel.setWallpaperFromUrl(
                                            it,
                                            context,
                                            listener
                                        )
                                    }
                                }
                        ) {
                            ElevatedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = combinedDataList[index].url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: @Composable () -> Unit,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            dialogText()
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}