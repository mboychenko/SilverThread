package com.allat.mboychenko.silverthread.presentation.cache

import android.content.Context
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util

class CacheDataSourceFactory(private val context: Context, private val cache: SimpleCache) : DataSource.Factory {
    private val defaultDatasourceFactory: DefaultDataSourceFactory

    init {
        val userAgent = Util.getUserAgent(this.context, "SilverThread")
        defaultDatasourceFactory = DefaultDataSourceFactory(
            this.context,
            DefaultHttpDataSourceFactory(userAgent)
        )
    }

    override fun createDataSource(): DataSource {
        return CacheDataSource(
            cache, defaultDatasourceFactory.createDataSource(),
            FileDataSource(), CacheDataSink(cache, CacheDataSink.DEFAULT_FRAGMENT_SIZE),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }
}