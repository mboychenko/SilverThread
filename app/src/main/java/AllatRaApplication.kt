package com.allat.mboychenko.silverthread

import android.app.Application
import android.content.Context
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.di.presentersModule
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.di.storageModule
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.downloader.PRDownloader
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.downloader.PRDownloaderConfig



class AllatRaApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val downloaderConfig = PRDownloaderConfig.newBuilder()
            .setReadTimeout(30_000)
            .setConnectTimeout(30_000)
            .setDatabaseEnabled(true)
            .build()

        PRDownloader.initialize(applicationContext, downloaderConfig)

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(listOf(storageModule, presentersModule))
        }

        FileLoaderService.commandRefreshLoadings(applicationContext)
    }


    companion object {
        fun get(context: Context): AllatRaApplication {
            return context.applicationContext as AllatRaApplication
        }
    }

}