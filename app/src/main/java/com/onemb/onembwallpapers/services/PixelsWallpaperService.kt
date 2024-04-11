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

data class CollectionResponse(
    val collections: List<Collection>,
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val next_page: String,
    val prev_page: String
)

data class Collection(
    val id: String,
    val title: String,
    val description: String?,
    val private: Boolean,
    val media_count: Int,
    val photos_count: Int,
    val videos_count: Int
)

data class Wallpaper(val name: String, val url: String)

data class Wallpapers(val wallpapers: Map<String, List<Wallpaper>>)


interface PixelsWallpaperService {

    @Headers("Authorization: 3cJI1Y0uEHUzCw7XroIGhEloabxbIYF7YLRBOMPsaZH0SPJFVHQZSOHL")
    @GET("v1/search?per_page=80")
    fun getWallpapers(@Query("page") page: Int, @Query("query") query: String): Call<WallpaperResponse>

    @Headers("Authorization: 3cJI1Y0uEHUzCw7XroIGhEloabxbIYF7YLRBOMPsaZH0SPJFVHQZSOHL")
    @GET("v1/collections/featured?per_page=50")
    fun getCollections(): Call<CollectionResponse>
}