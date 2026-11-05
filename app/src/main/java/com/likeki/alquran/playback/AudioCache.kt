package com.likeki.alquran.playback

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object AudioCache {

    private const val CACHE_SIZE_BYTES = 4L * 1024 * 1024 * 1024

    @Volatile private var instance: SimpleCache? = null

    fun get(context: Context): SimpleCache {
        return instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }
    }

    private fun build(ctx: Context): SimpleCache {
        val cacheDir = File(ctx.cacheDir, "audio_cache").apply { mkdirs() }
        return SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES),
            StandaloneDatabaseProvider(ctx)
        )
    }
}
