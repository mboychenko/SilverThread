package com.allat.mboychenko.silverthread

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.allat.mboychenko.silverthread.presentation.di.exoPlayerStorageModule
import com.allat.mboychenko.silverthread.presentation.di.presentersModule
import com.allat.mboychenko.silverthread.presentation.di.storageModule
import com.allat.mboychenko.silverthread.presentation.helpers.getPublicDownloadsStorageDir
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.utils.updateVersion
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AllatRaApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        //here is no useful info, just prevent UndeliverableException crash
        RxJavaPlugins.setErrorHandler {}

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(listOf(storageModule, presentersModule, exoPlayerStorageModule))
        }

        saveLogcatToFile()

        FileLoaderService.commandRefreshLoadings(applicationContext)

        updateVersion(this)

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun saveLogcatToFile() {
        val fileName = "/logcat_" + System.currentTimeMillis() + ".txt"
        val outputFile = getPublicDownloadsStorageDir("allat_logcat")
        Runtime.getRuntime().exec("logcat NotificationHelper:D AlarmManager:D LogQuotes:D *:S -f " + outputFile?.absolutePath + fileName)
    }

}