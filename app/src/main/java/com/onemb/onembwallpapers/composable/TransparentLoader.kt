package com.onemb.onembwallpapers.composable

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.onemb.onembwallpapers.utils.ScreenUtils.getScreenHeight

@Composable
fun TransparentLoader() {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        tonalElevation = 16.dp,
        shadowElevation = 16.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center).size(200.dp),
                strokeWidth = 4.dp
            )
        }
    }
}
