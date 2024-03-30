package com.onemb.onembwallpapers.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.random.Random

data class WallpaperResponse(
    val page: Int,
    val per_page: Int,
    val photos: List<WallpaperItem>,
    val next_page: String?
)

data class WallpaperItem(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    val photographer_url: String,
    val photographer_id: Int,
    val avg_color: String,
    val src: Src,
    val liked: Boolean,
    val alt: String
)

data class Src(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)

data class GalleryResponse(
    val id: String,
    val title: String,
    val description: String,
    val isPrivate: Boolean,
    val mediaCount: Int,
    val photosCount: Int,
    val videosCount: Int
)


interface PixelsWallpaperService {

    @Headers("Authorization: 3cJI1Y0uEHUzCw7XroIGhEloabxbIYF7YLRBOMPsaZH0SPJFVHQZSOHL")
    @GET("v1/curated?per_page=80")
    fun getWallpapers(@Query("page") page: Int): Call<WallpaperResponse>

    @Headers("Authorization: 3cJI1Y0uEHUzCw7XroIGhEloabxbIYF7YLRBOMPsaZH0SPJFVHQZSOHL")
    @GET("v1/curated?per_page=50")
    fun getCollections(): Call<GalleryResponse>
}