package com.onemb.onembwallpapers.composable

import android.content.Intent
import android.util.Log
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.services.Wallpaper
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.viewmodels.BitmapSetListener
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperApp(navController: NavController, viewModel: WallpaperViewModel) {
    val context = LocalContext.current
    val wallpapersState = viewModel.wallpapers.observeAsState()
    val combinedDataList = mutableListOf<Wallpaper>()
    val keys = viewModel.getSelectedCollection(context, context.getString(R.string.app_collection_key))
    val serviceRunning = remember{ mutableStateOf(false) }

    for (key in keys) {
        val index = wallpapersState.value?.indexOfFirst { wallpaperKey ->  wallpaperKey.wallpapers.containsKey(key)}!!
        val dataForCurrentKey = wallpapersState.value?.get(index)?.wallpapers?.get(key)
        if (dataForCurrentKey != null) {
            combinedDataList.addAll(dataForCurrentKey)
        }
    }
    val navigatedPreview = viewModel.navigatedPreview.collectAsState(initial = false).value

    LaunchedEffect(null) {
        if(viewModel.isForegroundServiceRunning(context)) {
            serviceRunning.value = true
        }
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
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.loadLocalJson(context)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Localized description"
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
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Back to Category")
            }
        }
    ) { innerPadding ->
        val listener = object : BitmapSetListener {
            override suspend fun onBitmapSet() {
                Log.d("APPP", "APPP")
                if(!navigatedPreview) {
                    viewModel.setNavigatedPreview(true)
                    navController.navigate("Preview")
                }
            }
            override fun onBitmapSetError(error: Throwable) {

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
                                    if (!serviceRunning.value) {
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
                                            serviceRunning.value = false
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
                                            serviceRunning.value = false
                                        }
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
                                        fontWeight = FontWeight.Light
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
