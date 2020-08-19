package com.allat.mboychenko.silverthread

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.multidex.MultiDex
import androidx.work.*
import com.allat.mboychenko.silverthread.presentation.di.*
import com.allat.mboychenko.silverthread.presentation.services.AllatRadioService
import com.allat.mboychenko.silverthread.presentation.services.EveryDayWork
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.utils.updateVersion
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //here is no useful info, just prevent UndeliverableException crash
        RxJavaPlugins.setErrorHandler {}

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(
                listOf(
                    storageModule,
                    dbModule,
                    androidModule,
                    repositoryModule,
                    useCaseModule,
                    presentersModule,
                    viewModelsModule,
                    exoPlayerStorageModule,
                    helpers
                )
            )
        }

        FileLoaderService.commandRefreshLoadings(applicationContext)

        val workManager: WorkManager by inject()
        updateVersion(this, workManager)
        scheduleEveryDayWork(workManager)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun scheduleEveryDayWork(workManager: WorkManager) {
        EveryDayWork.initWork(workManager, applicationContext)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopService(Intent(applicationContext, AllatRadioService::class.java)
            .apply { action = AllatRadioService.ACTION_STOP })
    }

}