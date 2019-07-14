package com.allat.mboychenko.silverthread

import android.app.Application
import com.allat.mboychenko.silverthread.presentation.di.exoPlayerStorageModule
import com.allat.mboychenko.silverthread.presentation.di.presentersModule
import com.allat.mboychenko.silverthread.presentation.di.storageModule
import com.allat.mboychenko.silverthread.presentation.helpers.getPublicDownloadsStorageDir
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AllatRaApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(listOf(storageModule, presentersModule, exoPlayerStorageModule))
        }

        saveLogcatToFile()

        FileLoaderService.commandRefreshLoadings(applicationContext)
    }

    private fun saveLogcatToFile() {
        val fileName = "/logcat_" + System.currentTimeMillis() + ".txt"
        val outputFile = getPublicDownloadsStorageDir("allat_logcat")
        Runtime.getRuntime().exec("logcat UpdateBeforeTimerJob:D NotificationTimer:D LogQuotes:D *:S -f " + outputFile?.absolutePath + fileName)
    }

}