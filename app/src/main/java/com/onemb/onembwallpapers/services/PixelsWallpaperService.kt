package com.onemb.onembwallpapers.services





data class Wallpaper(val name: String, val url: String) {
    // Deep copy function for Wallpaper
    fun deepCopy(): Wallpaper {
        // Simply return a new instance with the same fields
        return Wallpaper(name, url)
    }
}

data class Wallpapers(val wallpapers: Map<String, List<Wallpaper>>) {
    fun deepCopy(): Wallpapers {
        val clonedMap = wallpapers.mapValues { (_, value) ->
            value.map { it.deepCopy() }
        }
        return Wallpapers(clonedMap)
    }
}
