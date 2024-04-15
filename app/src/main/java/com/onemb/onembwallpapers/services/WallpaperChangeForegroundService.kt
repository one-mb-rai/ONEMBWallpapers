package com.onemb.onembwallpapers.services
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.request.ImageRequest
import com.onemb.onembwallpapers.MainActivity
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.receivers.ONEMBReceiver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class WallpaperChangeForegroundService : Service() {

    private var wallpapers: List<Wallpapers> = emptyList()

    private var collections: List<String> = emptyList()

    private val generatedNumbers = mutableSetOf<Int>()


    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ScreenUnlockServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    private var isServiceRunning = false
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
    @SuppressLint("MissingPermission", "LaunchActivityFromNotification")
    private fun startForegroundService() {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val intent = Intent(this, ONEMBReceiver::class.java)
        intent.action = "ACTION_STOP_SERVICE"
        val pendingIntentStopService = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Wallpaper change service")
            .setContentText("Service is running with minimal power")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null).setVibrate(longArrayOf(0))
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                R.drawable.baseline_stop_24,
                "Stop Service",
                pendingIntentStopService
            )
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

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("ONEMBCollectionPreferences", Context.MODE_PRIVATE)
    }

    private fun startFunction(context: Context) {
        Thread {
            while (isServiceRunning) {
                try {
                    val inputStream: InputStream = context.assets.open("fileList.json")
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    val json = String(buffer, charset("UTF-8"))

                    // Parse the JSON string
                    val jsonObject = JSONObject(json)
                    val wallpapersObject = jsonObject.getJSONObject("wallpapers")
                    val fileList = mutableListOf<Wallpapers>()
                    val keysList = mutableListOf<String>()

                    // Iterate through each folder
                    val keys = wallpapersObject.keys()
                    while (keys.hasNext()) {
                        val folderName = keys.next()
                        val jsonArray = wallpapersObject.getJSONArray(folderName)
                        keysList.add(folderName)

                        // Iterate through wallpapers in the folder
                        val wallpapersList = mutableListOf<Wallpaper>()
                        for (i in 0 until jsonArray.length()) {
                            val wallpaperObject = jsonArray.getJSONObject(i)
                            val fileName = wallpaperObject.getString("name")
                            val fileUrl = wallpaperObject.getString("url")
                            val wallpaper = Wallpaper(fileName, fileUrl)
                            wallpapersList.add(wallpaper)
                        }

                        // Add wallpapers list to the result
                        fileList.add(Wallpapers(mapOf(folderName to wallpapersList)))
                    }

                    collections = keysList
                    // Set the result to LiveData or do whatever you need to do with it
                    wallpapers = fileList
                    startWallpaperSetProcess(wallpapers, context)

                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                // Sleep for a specified interval
                Thread.sleep(1000 * 60 * 30)
            }
        }.start()
    }


    private fun getSelectedCollection(context: Context, listName: String): List<String> {
        val sharedPreferences = getSharedPreferences(context)
        val selectedCollectionSet = sharedPreferences.getStringSet(listName, emptySet())
        return selectedCollectionSet?.toList() ?: emptyList()
    }

    private fun generateUniqueRandomNumber(generatedNumbers: MutableSet<Int>, maxValue: Int): Int {
        while (true) {
            val randomNumber = Random.nextInt(maxValue)
            if (randomNumber !in generatedNumbers) {
                generatedNumbers.add(randomNumber)
                return randomNumber
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startWallpaperSetProcess(wallpapers: List<Wallpapers>?, context: Context) {
        val deepClonedWallpapers = wallpapers?.map { it.deepCopy() }

        val randomIndex = generateUniqueRandomNumber(generatedNumbers, deepClonedWallpapers?.size!!)

        val combinedDataList = mutableListOf<Wallpaper>()
        val keys = getSelectedCollection(context, context.getString(R.string.app_collection_key))

        for (key in keys) {
            val index = deepClonedWallpapers.indexOfFirst { wallpaperKey ->  wallpaperKey.wallpapers.containsKey(key)}
            val dataForCurrentKey = deepClonedWallpapers[index].wallpapers[key]
            if (dataForCurrentKey != null) {
                combinedDataList.addAll(dataForCurrentKey)
            }
        }
        combinedDataList.shuffle()
        val bitmap = combinedDataList[randomIndex].url
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
                Toast.makeText(context, "Wallpaper has changed", 1000 * 3).show()
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
