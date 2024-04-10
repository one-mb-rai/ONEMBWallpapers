package com.onemb.onembwallpapers.services
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.onemb.onembwallpapers.MainActivity
import com.onemb.onembwallpapers.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.random.Random

class WallpaperChangeForegroundService : Service() {
    private val retrofit: Retrofit = Retrofit.Builder().
        baseUrl("https://api.pexels.com/").
        addConverterFactory(GsonConverterFactory.create()).
        build();
    private var service = retrofit.create(PixelsWallpaperService::class.java)
    private var wallpaperResponse: WallpaperResponse? = null
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ScreenUnlockServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    private var isServiceRunning = false
    private var index = 0
    private var id = ""

    /**
     * Returns null since the service does not provide binding.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Called when the service is created. Initiates foreground service and registers
     * the screen unlock receiver.
     */
    override fun onCreate() {
        super.onCreate()

        startForegroundService()
    }

    /**
     * Called when the service is destroyed. Unregisters the screen unlock receiver.
     */
    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }

    /**
     * Starts the service in the foreground, creates a notification channel, and
     * displays a notification with low priority.
     */
    private fun startForegroundService() {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Wallpaper change service")
            .setContentText("Service is running with minimal power")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null).setVibrate(longArrayOf(0))
            .build()

        // Check for notification permission before notifying
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
        startFunction(this)
        isServiceRunning = true
    }

    private fun startFunction(context: Context) {
        Thread {
            while (isServiceRunning) {
                if(wallpaperResponse != null) {
                    startWallpaperSetProcess(wallpaperResponse!!, context)
                } else {
//                    val call: Call<WallpaperResponse> =
//                        service.getWallpapers(Random.nextInt(1, 100))
//                    call.enqueue(object : Callback<WallpaperResponse> {
//                        override fun onResponse(
//                            call: Call<WallpaperResponse>,
//                            response: Response<WallpaperResponse>
//                        ) {
//                            if (response.isSuccessful) {
//                                val wallpapers: WallpaperResponse? = response.body()
//                                if (wallpapers != null) {
//                                    wallpaperResponse = wallpapers
//                                }
//                                if (wallpapers != null) {
//                                    startWallpaperSetProcess(wallpapers, context)
//                                }
//                                Log.d("Response", wallpapers?.photos?.size.toString())
//                            } else {
//                                Log.d(
//                                    "HTTP Error",
//                                    "Failed to fetch wallpapers: ${response.code()}"
//                                )
//                            }
//                        }
//
//                        override fun onFailure(p0: Call<WallpaperResponse>, p1: Throwable) {
//                            Log.d("Network Error", "Error fetching wallpapers: ${p1.message}")
//                        }
//                    })
                }

                // Sleep for a specified interval
                Thread.sleep(1000 * 60 * 30)
            }
        }.start()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startWallpaperSetProcess(wallpapers: WallpaperResponse, context: Context) {
        index = Random.nextInt(1, 60)
        val bitmap = wallpapers.photos[index].src.original
        id = wallpapers.photos[index].photographer_id.toString()
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val bitmapFile = loadImageBitmap(bitmap, context)
                setWallpaperFromService(bitmapFile, context)
            } catch (e: IOException) {
                Log.e(
                    "WallpaperViewModel",
                    "Error loading image bitmap: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadImageBitmap(imageUrl: String, context: Context): Bitmap {
        return withContext(Dispatchers.IO) {
            val imageLoader = ImageLoader.Builder(context)
                .build()

            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()

            try {
                imageLoader.execute(request).drawable?.toBitmap() ?: throw IOException("Bitmap is null")
            } catch (e: Exception) {
                throw IOException("Error loading bitmap: ${e.message}")
            }
        }
    }

    private suspend fun setWallpaperFromService(bitmap: Bitmap, context: Context) {
        withContext(Dispatchers.Main) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            try {
                wallpaperManager.setBitmap(bitmap)
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                Toast.makeText(context, "Wallpaper changed", 1000 * 3).show()
                wallpaperResponse = if(wallpaperResponse?.photos?.isEmpty() == true) {
                    null
                } else {
                    wallpaperResponse?.photos?.filter { it.photographer_id.toString() != id }
                        ?.let { wallpaperResponse?.copy(photos = it) }
                }
                Log.d("WallpaperViewModel", "Wallpaper set successfully")
            } catch (e: IOException) {
                Log.e("WallpaperViewModel", "Error setting wallpaper: ${e.message}")
            }
        }
    }

    /**
     * Creates a notification channel for the foreground service.
     */
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }
}
