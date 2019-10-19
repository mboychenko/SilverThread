package com.allat.mboychenko.silverthread

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import android.app.Application
import android.content.Context
import io.reactivex.plugins.RxJavaPlugins
import com.allat.mboychenko.silverthread.presentation.di.exoPlayerStorageModule
import com.allat.mboychenko.silverthread.presentation.di.presentersModule
import com.allat.mboychenko.silverthread.presentation.di.storageModule
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.utils.updateVersion
import androidx.multidex.MultiDex

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

        FileLoaderService.commandRefreshLoadings(applicationContext)

        updateVersion(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}