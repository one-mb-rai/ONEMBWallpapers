package com.onemb.onembwallpapers.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.onemb.onembwallpapers.services.WallpaperChangeForegroundService

class ONEMBReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_STOP_SERVICE") {
            val serviceIntent = Intent(
                context,
                WallpaperChangeForegroundService::class.java
            )
            context.stopService(serviceIntent)
        }
    }
}
