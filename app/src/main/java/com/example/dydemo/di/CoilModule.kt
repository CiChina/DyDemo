package com.example.dydemo.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.util.DebugLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoilModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512 * 1024 * 1024)
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(Cache(File(context.cacheDir, "okhttp_cache"), 512 * 1024 * 1024))
                    .dispatcher(Dispatcher().apply {
                        maxRequestsPerHost = 20
                    })
                    .addInterceptor(RobustRetryInterceptor())
                    .build()
            }
            .logger(DebugLogger())
            .respectCacheHeaders(false)
            .build()
    }
}

/**
 * A robust interceptor that retries on both unsuccessful HTTP responses AND network I/O exceptions.
 */
private class RobustRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null

        var tryCount = 0
        val maxRetries = 3

        while (tryCount < maxRetries) {
            try {
                response?.close() // Close previous response if it exists
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
            }

            tryCount++

            if (tryCount < maxRetries) {
                runBlocking { delay(1000 * tryCount.toLong()) }
            }
        }

        // If we're here, all retries have failed.
        // Throw the last-captured exception, or a generic one.
        throw exception ?: IOException("Image request failed after $maxRetries retries")
    }
}
