package com.example.amls.ml

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@androidx.media3.common.util.UnstableApi
@Singleton
class VideoCacheManager @Inject constructor(
    @ApplicationContext context: Context
) {
    // 500 MB de espacio máximo para videos cacheados — cuando se llena,
    // se borran automáticamente los menos usados recientemente (LRU).
    private val tamanoMaximoCache: Long = 500L * 1024 * 1024

    val cache: SimpleCache by lazy {
        SimpleCache(
            File(context.cacheDir, "video_cache"),
            LeastRecentlyUsedCacheEvictor(tamanoMaximoCache),
            StandaloneDatabaseProvider(context)
        )
    }
}
