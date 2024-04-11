package com.onemb.onembwallpapers.viewmodels

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.onemb.onembwallpapers.R
import com.onemb.onembwallpapers.services.CollectionResponse
import com.onemb.onembwallpapers.services.JSONResponse
import com.onemb.onembwallpapers.services.PixelsWallpaperService
import com.onemb.onembwallpapers.services.WallpaperResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random


class WallpaperViewModel : ViewModel() {
    private val retrofit: Retrofit = Retrofit.Builder().
                             baseUrl("https://api.pexels.com/").
                             addConverterFactory(GsonConverterFactory.create()).
                             build();
    private var service = retrofit.create(PixelsWallpaperService::class.java)

    private val _wallpapers = MutableLiveData<List<JSONResponse>>()
    val wallpapers: MutableLiveData<List<JSONResponse>> = _wallpapers

    private val _wallpapersBitmapLoaded = MutableLiveData(false)
    val wallpapersBitmapLoaded: MutableLiveData<Boolean> = _wallpapersBitmapLoaded

    private var wallpaperBitmap: Bitmap? = null

    init {
//        getWallpapersCategories()
    }

    fun loadLocalJson(context: Context) {
        try {
            val inputStream: InputStream = context.getAssets().open("fileList.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, charset("UTF-8"))
            val fileList = mutableListOf<JSONResponse>()

            // Parse the JSON string
            val jsonArray = JSONArray(json)
            Log.d("ARRAY", jsonArray.toString())
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val fileName = jsonObject.getString("name")
                val fileUrl = jsonObject.getString("url")
                val jsonResponse = JSONResponse(fileName, fileUrl)
                fileList.add(jsonResponse)
            }
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

    private fun getRandomNumber(): Int {
        return Random.nextInt(1, 100)
    }

    fun getWallpaperBitmap(): Bitmap? {
        return wallpaperBitmap
    }

    fun setWallpapersBitmapLoaded(value: Boolean) {
        _wallpapersBitmapLoaded.value = value
    }

    fun setWallpaperFromUrl(imageUrl: String, context: Context) {
        viewModelScope.launch {
            try {
                val bitmap = loadImageBitmap(imageUrl, context)
                wallpaperBitmap = bitmap
                setWallpapersBitmapLoaded(true)
            } catch (e: IOException) {
                Log.e("WallpaperViewModel", "Error loading image bitmap: ${e.message}")
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

    suspend fun setWallpaper(bitmap: Bitmap, context: Context) {
        withContext(Dispatchers.IO) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            try {
                wallpaperManager.setBitmap(bitmap)
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                Log.d("WallpaperViewModel", "Wallpaper set successfully")
            } catch (e: IOException) {
                Log.e("WallpaperViewModel", "Error setting wallpaper: ${e.message}")
            }
        }
    }

}
