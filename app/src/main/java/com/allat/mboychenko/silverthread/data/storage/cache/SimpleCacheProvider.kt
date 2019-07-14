package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.storage.cache

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

fun provideExoPlayerCache(context: Context): SimpleCache {
    return SimpleCache(File(context.cacheDir, "media"),
        LeastRecentlyUsedCacheEvictor(CacheDataSink.DEFAULT_FRAGMENT_SIZE * 10),
        ExoDatabaseProvider(context))
}
