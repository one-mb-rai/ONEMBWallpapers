package com.onemb.onembwallpapers.receivers

import com.onemb.onembwallpapers.ONEMBApplication
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel

class ONEMBReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app: ONEMBApplication = context.applicationContext as ONEMBApplication
        val viewModel: WallpaperViewModel = app.wallpaperViewModel
        if (intent.action == "ACTION_STOP_SERVICE") {
            val serviceIntent = Intent(
                context,
                WallpaperChangeForegroundService::class.java
            )
            viewModel.setServiceRunning(false)
            context.stopService(serviceIntent)
        }
        if (intent.action == "PERMISSION_GRANTED") {
            viewModel.setLoading(true)
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
            viewModel.setServiceRunning(true)
            context.startService(serviceIntent)
        }
        if (intent.action == "START_LOADING") {
            viewModel.setLoading(true)
        }
        if (intent.action == "STOP_LOADING") {
            viewModel.setLoading(false)
        }
    }
}
