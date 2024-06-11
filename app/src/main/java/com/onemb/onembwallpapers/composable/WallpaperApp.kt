package com.onemb.onembwallpapers.composable

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.WorkManager
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.onemb.onembwallpapers.ONEMBApplication.getInstance
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.services.Wallpaper
import com.onemb.onembwallpapers.services.WallpaperChangeWorker
import com.onemb.onembwallpapers.utils.ScreenUtils
import com.onemb.onembwallpapers.utils.ScreenUtils.isWallpaperChangeWorkerEnqueued
import com.onemb.onembwallpapers.viewmodels.BitmapSetListener
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperApp(
    navController: NavController,
    viewModel: WallpaperViewModel
) {
    val context = LocalContext.current
    val wallpapersState = viewModel.wallpapers.observeAsState()
    val combinedDataList = mutableListOf<Wallpaper>()
    val keys = viewModel.getSelectedCollection(context, context.getString(R.string.app_collection_key))

    for (key in keys) {
        val index = wallpapersState.value?.indexOfFirst { wallpaperKey ->  wallpaperKey.wallpapers.containsKey(key)}!!
        val dataForCurrentKey = wallpapersState.value?.get(index)?.wallpapers?.get(key)
        if (dataForCurrentKey != null) {
            combinedDataList.addAll(dataForCurrentKey)
        }
    }
    val navigatedPreview = viewModel.navigatedPreview.collectAsState(initial = false).value
    var visibleItemCount by remember { mutableStateOf(12) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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

        val isWorkerEnqueued = remember { mutableStateOf(false) }

        LaunchedEffect(null) {
            isWorkerEnqueued.value = isWallpaperChangeWorkerEnqueued(context)
        }

        Box {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(innerPadding)
            ) {
                val visibleItems = combinedDataList.take(visibleItemCount)

                items(visibleItems.size) { index ->
                    val item = visibleItems[index]
                    val lastIndex = visibleItems.indexOfLast { it == item }
                    if (lastIndex == visibleItems.size - 1) {
                        visibleItemCount += 12
                    }

                    if (index == 0) {
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
                                            WallpaperChangeWorker.enqueueWallpaperChangeWork(getInstance().applicationContext, "Home")
                                            isWorkerEnqueued.value = true
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
                                            WallpaperChangeWorker.enqueueWallpaperChangeWork(getInstance().applicationContext, "Lock")
                                            isWorkerEnqueued.value = true
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
                                            WallpaperChangeWorker.enqueueWallpaperChangeWork(getInstance().applicationContext, "Both")
                                            isWorkerEnqueued.value = true
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
                            modifier = Modifier
                                .padding(3.dp)
                                .fillMaxSize()
                                .height(200.dp)
                                .clickable {
                                    if (!isWorkerEnqueued.value) {
                                        showBottomSheet = true
                                    } else {
                                        WorkManager
                                            .getInstance(context)
                                            .cancelAllWorkByTag(WallpaperChangeWorker.WORK_TAG);
                                        isWorkerEnqueued.value = false
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
                                        text = if(isWorkerEnqueued.value) "Stop Service" else "Change every 30 minutes",
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
                            var cardWidth by remember { mutableStateOf(0) }
                            var cardHeight by remember { mutableStateOf(0) }
                            ElevatedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned { coordinates ->
                                        cardWidth = coordinates.size.width
                                        cardHeight = coordinates.size.height
                                    }
                            ) {
                                if(cardWidth > 0 && cardHeight > 0 ) {
                                    val painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(combinedDataList[index].url)
                                            .size(Size((cardWidth + 200), (cardHeight + 100)))
                                            .build()
                                    )
                                    if (painter.state is AsyncImagePainter.State.Loading) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                    Image(
                                        painter = painter,
                                        contentDescription = "",
                                        contentScale = ContentScale.None
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}