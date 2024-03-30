package com.onemb.onembwallpapers.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperCategory(navController: NavController, viewModel: WallpaperViewModel) {

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Wallpaper Category",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        },
    ) { innerPadding ->
        val context = LocalContext.current
        val wallpapersState = viewModel.wallpapers.observeAsState()
        val bitmapLoaded = viewModel.wallpapersBitmapLoaded.observeAsState()
        val arrayIndex = remember {
            mutableIntStateOf(0)
        }
        LaunchedEffect(true) {
            viewModel.getWallpapers()
        }

        when {
            bitmapLoaded.value == true -> {
                viewModel.setWallpapersBitmapLoaded(false)
                navController.navigate("preview")
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.padding(innerPadding)) {
            wallpapersState.value?.photos?.let { wallpapers ->
                itemsIndexed(wallpapers) { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(3.dp).fillMaxSize().height(200.dp)
                            .clickable {
                                arrayIndex.intValue = index
                                wallpapersState.value?.photos?.get(index)?.src?.original?.let {
                                    viewModel.setWallpaperFromUrl(
                                        it,
                                        context
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
                                model = wallpapersState.value?.photos?.get(index)?.src?.large,
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
