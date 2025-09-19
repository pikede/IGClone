package com.example.instagram

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.example.instagram.core_ui_components.image.CoilUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InstagramApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        CoilUtil.buildImageLoader(context)
}