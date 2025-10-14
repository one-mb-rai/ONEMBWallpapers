package com.onemb.onembwallpapers.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.core.content.ContextCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.onemb.onembwallpapers.services.WallpaperChangeWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object ScreenUtils {
    fun getScreenWidth(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getScreenHeight(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.height()
        } else {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    suspend fun isWallpaperChangeWorkerEnqueued(context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = withContext(Dispatchers.IO) {
            workManager.getWorkInfosByTag(WallpaperChangeWorker.WORK_TAG).get()
        }
        for (workInfo in workInfos) {
            if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }
        return false
    }
}
