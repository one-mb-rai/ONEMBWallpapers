package com.onemb.onembwallpapers.services

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.work.*
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import com.onemb.onembwallpapers.ONEMBApplication
import com.onemb.onembwallpapers.ONEMBApplication.getInstance
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.utils.ScreenUtils.isWallpaperChangeWorkerEnqueued
import com.onemb.onembwallpapers.viewmodels.WallpaperViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class WallpaperChangeWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private var wallpapers: List<Wallpapers> = emptyList()

    private var collections: List<String> = emptyList()

    private val generatedNumbers = mutableSetOf<Int>()

    override fun doWork(): Result {
        return try {
            val context = getInstance().applicationContext
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
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun getSelectedCollection(context: Context, listName: String): List<String> {
        val sharedPreferences = getSharedPreferences(context)
        val selectedCollectionSet = sharedPreferences.getStringSet(listName, emptySet())
        return selectedCollectionSet?.toList() ?: emptyList()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("ONEMBCollectionPreferences", Context.MODE_PRIVATE)
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
                val app: ONEMBApplication = getInstance().applicationContext as ONEMBApplication
                val viewModel: WallpaperViewModel = app.wallpaperViewModel
                viewModel.setLoading(false)
                Log.d("WallpaperViewModel", "Wallpaper set successfully")
            } catch (e: IOException) {
                Log.e("WallpaperViewModel", "Error setting wallpaper: ${e.message}")
            }
        }
    }


    companion object {
        private const val WORK_NAME = "WallpaperChangeWorker"

        const val WORK_TAG = "ONEMB"
        fun enqueueWallpaperChangeWork(context: Context) {
            if(!isWallpaperChangeWorkerEnqueued(context)) {
                val workRequest = PeriodicWorkRequestBuilder<WallpaperChangeWorker>(
                    repeatInterval = 30,
                    repeatIntervalTimeUnit = TimeUnit.MINUTES,
                ).addTag(WORK_TAG).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }
}
