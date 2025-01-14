package com.onemb.onembwallpapers.viewmodels

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.onemb.onembwallpapers.services.Wallpaper
import com.onemb.onembwallpapers.services.Wallpapers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

interface BitmapSetListener {
    suspend fun onBitmapSet()
    fun onBitmapSetError(error: Throwable)
}

class WallpaperViewModel : ViewModel() {

    private val _wallpapers = MutableLiveData<List<Wallpapers>>()
    val wallpapers: MutableLiveData<List<Wallpapers>> = _wallpapers

    private val _collections = MutableLiveData<MutableList<String>?>()
    val collections: MutableLiveData<MutableList<String>?> = _collections

    private val _selectedCollection = MutableLiveData<List<String>?>()
    val selectedCollection: MutableLiveData<List<String>?> = _selectedCollection

    private var wallpaperBitmap: Bitmap? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading

    private val _wallpaperSet = MutableStateFlow(false)
    val wallpaperSet: Flow<Boolean> = _wallpaperSet

    private val _navigatedPreview = MutableStateFlow(false)
    val navigatedPreview: Flow<Boolean> = _navigatedPreview

    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: Flow<Boolean> = _onboardingDone


    fun setOnboarding(value: Boolean) {
        _onboardingDone.value = value
    }

    fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    fun setNavigatedPreview(value: Boolean) {
        _navigatedPreview.value = value
    }

    fun wallpaperSet(value: Boolean) {
        _wallpaperSet.value = value
    }

    fun loadLocalJson(context: Context) {
        try {
            val inputStream: InputStream = context.assets.open("fileList.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, charset("UTF-8"))

            // Parse the JSON string
            val jsonObject = JSONObject(json)
            val wallpapersObject = jsonObject.getJSONObject("wallpapersArray")
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
                wallpapersList.shuffle()

                // Add wallpapers list to the result
                fileList.add(Wallpapers(mapOf(folderName to wallpapersList)))
            }

            _collections.value = keysList
            // Set the result to LiveData or do whatever you need to do with it
            _wallpapers.value = fileList


        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("ONEMBCollectionPreferences", Context.MODE_PRIVATE)
    }

    // Function to save selectedCollection to SharedPreferences
    fun saveSelectedCollection(context: Context, selectedCollection: List<String>, listName: String) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putStringSet(listName, selectedCollection.toSet())
        editor.apply()
        _selectedCollection.value = selectedCollection
    }

    // Function to retrieve selectedCollection from SharedPreferences
    fun getSelectedCollection(context: Context, listName: String): List<String> {
        val sharedPreferences = getSharedPreferences(context)
        val selectedCollectionSet = sharedPreferences.getStringSet(listName, emptySet())
        return selectedCollectionSet?.toList() ?: emptyList()
    }

    @SuppressLint("CommitPrefEdits")
    fun removeSelectedCollection(context: Context, listName: String) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().remove(listName)
    }


    fun getWallpaperBitmap(): Bitmap? {
        return wallpaperBitmap
    }


    fun setWallpaperFromUrl(imageUrl: String, context: Context, listener: BitmapSetListener) {
        viewModelScope.launch {
            try {
                val bitmap = loadImageBitmap(imageUrl, context)
                wallpaperBitmap = bitmap
                listener.onBitmapSet()
            } catch (e: IOException) {
                listener.onBitmapSetError(e)
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

    suspend fun saveBitmapAndGetUri(bitmap: Bitmap, context: Context, fileName: String): Uri? {
        return withContext(Dispatchers.IO) {
            val folder = File(Environment.getExternalStorageDirectory(), "ONEMBWallpapers")
            if (!folder.exists()) {
                folder.mkdirs()
            }

            val file = File(folder, fileName)
            try {
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }
                Uri.fromFile(file)
            } catch (e: IOException) {
                Log.e("WallpaperViewModel", "Error saving bitmap: ${e.message}")
                null
            }
        }
    }

    suspend fun setWallpaper(bitmap: Bitmap, context: Context, setOn: String) {
        withContext(Dispatchers.IO) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            try {
                setLoading(true)
                when(setOn) {
                    "home" -> wallpaperManager.setBitmap(bitmap)
                    "lockScreen" -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    "both" -> {
                        wallpaperManager.setBitmap(bitmap)
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                }
                setLoading(false)
                wallpaperSet(true)
            } catch (e: IOException) {
                Log.e("WallpaperViewModel", "Error setting wallpaper: ${e.message}")
            }
        }
        withContext(Dispatchers.Main) {
            Toast
                .makeText(
                    context,
                    "Wallpaper change successful",
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

}
