package com.example.gruya

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.gruya.data.sync.SyncHandler
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class GruYaApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var syncHandler: SyncHandler

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(false)
            .build()
    }
}
