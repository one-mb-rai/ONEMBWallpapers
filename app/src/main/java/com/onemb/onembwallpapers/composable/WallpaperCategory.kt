package com.onemb.onembwallpapers.composable

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperCategory(navController: NavController, viewModel: WallpaperViewModel) {
    val collectionsState = viewModel.collections.observeAsState()
    var selectedCollection by remember { mutableStateOf(emptyList<String>()) }
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
                        text = "Wallpaper Category",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveSelectedCollection(context, selectedCollection, context.getString(R.string.app_collection_key))
                    Toast.makeText(context, "Wallpaper change service stopped", 1000 * 3).show()
                    val serviceIntent = Intent(
                        context,
                        WallpaperChangeForegroundService::class.java
                    )
                    context.stopService(serviceIntent)
                    navController.navigate("Home")
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        }
    ) { innerPadding ->

        LaunchedEffect (null){
            if(viewModel.getSelectedCollection(context, context.getString(R.string.app_collection_key)).isNotEmpty()) {
                selectedCollection = viewModel.getSelectedCollection(context, context.getString(R.string.app_collection_key))
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(innerPadding)) {
            collectionsState.value?.let { item ->
                itemsIndexed(item) { index, _ ->
                    Row(
                        modifier = Modifier
                            .padding(2.dp).height(50.dp)
                    ) {
                        ElevatedFilterChip(
                            modifier = Modifier.fillMaxSize(),
                            onClick = {
                                selectedCollection = if (selectedCollection.contains(collectionsState.value!![index])) {
                                    selectedCollection - collectionsState.value!![index]
                                } else {
                                    selectedCollection + collectionsState.value!![index]
                                }
                                Log.d("DATA", selectedCollection.joinToString(", "))
                            },
                            label = {
                                Text(
                                    collectionsState.value!![index],
                                )
                            },
                            selected = selectedCollection.contains(collectionsState.value!![index]),
                            leadingIcon = if(selectedCollection.contains(collectionsState.value!![index])) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Done icon",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }
}