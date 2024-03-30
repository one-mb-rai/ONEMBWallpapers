package com.onemb.onembwallpapers.viewmodels

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.onemb.onembwallpapers.services.PixelsWallpaperService
import com.onemb.onembwallpapers.services.WallpaperResponse
import com.onemb.onembwallpapers.utils.ScreenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class WallpaperViewModel : ViewModel() {
    private val retrofit: Retrofit = Retrofit.Builder().
                             baseUrl("https://api.pexels.com/").
                             addConverterFactory(GsonConverterFactory.create()).
                             build();
    private var service = retrofit.create(PixelsWallpaperService::class.java)

    private val _wallpapers = MutableLiveData<WallpaperResponse?>()
    val wallpapers: MutableLiveData<WallpaperResponse?> = _wallpapers

    private val _wallpapersBitmapLoaded = MutableLiveData(false)
    val wallpapersBitmapLoaded: MutableLiveData<Boolean> = _wallpapersBitmapLoaded

    private var wallpaperBitmap: Bitmap? = null

    init {
        getWallpapers()
    }


    private fun getRandomNumber(): Int {
        return Random.nextInt(1, 100)
    }

    fun getWallpapers() {
        val call: Call<WallpaperResponse> = service.getWallpapers(getRandomNumber())

        call.enqueue(object : Callback<WallpaperResponse> {
            override fun onResponse(call: Call<WallpaperResponse>, response: Response<WallpaperResponse>) {
                if (response.isSuccessful) {
                    val wallpapers: WallpaperResponse? = response.body()
                    _wallpapers.value = wallpapers
                    Log.d("Response", wallpapers?.photos?.size.toString())
                } else {
                    Log.d("HTTP Error", "Failed to fetch wallpapers: ${response.code()}")
                }
            }

            override fun onFailure(p0: Call<WallpaperResponse>, p1: Throwable) {
                Log.d("Network Error", "Error fetching wallpapers: ${p1.message}")
            }
        })
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
