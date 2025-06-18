package com.example.instagram.core_ui_components.images

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache

object CoilUtil {

    fun buildImageLoader(context: Context, cacheFolder: String = "image_cache"): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(cacheFolder))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}