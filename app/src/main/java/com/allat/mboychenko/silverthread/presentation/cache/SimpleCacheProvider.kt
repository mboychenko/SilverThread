package com.allat.mboychenko.silverthread.presentation.cache

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

fun provideExoPlayerCache(context: Context): SimpleCache {
    return SimpleCache(File(context.cacheDir, "media"),
        LeastRecentlyUsedCacheEvictor(CacheDataSink.DEFAULT_FRAGMENT_SIZE * 2),
        ExoDatabaseProvider(context))
}
