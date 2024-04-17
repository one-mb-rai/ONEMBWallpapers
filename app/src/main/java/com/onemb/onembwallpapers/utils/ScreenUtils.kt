package com.onemb.onembwallpapers.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.onemb.onembwallpapers.services.WallpaperChangeWorker


object ScreenUtils {
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun isWallpaperChangeWorkerEnqueued(context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(WallpaperChangeWorker.WORK_TAG).get()

        for (workInfo in workInfos) {
            if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }
        return false
    }
}

