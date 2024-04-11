package com.onemb.onembwallpapers.composable

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
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
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.utils.ScreenUtils
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperApp(navController: NavController, viewModel: WallpaperViewModel) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
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
                }
            )
        }
    ) { innerPadding ->
        val wallpapersState = viewModel.wallpapers.observeAsState()

        Log.d("Wallpapersda", wallpapersState.value?.get(0)?.wallpapers?.get("32k_wallpaper").toString())
        val arrayIndex = remember {
            mutableIntStateOf(0)
        }

        LaunchedEffect (null){
            viewModel.setWallpapersBitmapLoaded(false)
        }


        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.padding(innerPadding)) {
            wallpapersState.value?.get(0)?.wallpapers?.get("32k_wallpaper")
                ?.let { wallpapers ->
                itemsIndexed(wallpapers) { index, _ ->
                    if(index == 0) {
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .fillMaxSize()
                                .height(200.dp)
                                .clickable {
                                    viewModel.viewModelScope.launch {
                                        val serviceIntent = Intent(
                                            context,
                                            WallpaperChangeForegroundService::class.java
                                        )
                                        context.startService(serviceIntent)
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
                                        text = "Change every 30 minutes",
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
                                    arrayIndex.intValue = index
                                    wallpapersState.value?.get(0)?.wallpapers?.get("32k_wallpaper")!![index].url.let {
                                            viewModel.setWallpaperFromUrl(
                                                it,
                                                context
                                            )
                                        }
                                    navController.navigate("Preview")
                                }
                        ) {
                            ElevatedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = wallpapersState.value?.get(0)?.wallpapers?.get("32k_wallpaper")!![index].url,
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
