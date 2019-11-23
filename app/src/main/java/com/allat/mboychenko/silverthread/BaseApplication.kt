package com.allat.mboychenko.silverthread

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.multidex.MultiDex
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.allat.mboychenko.silverthread.presentation.di.*
import com.allat.mboychenko.silverthread.presentation.services.AllatRadioService
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.presentation.services.UpdateTimersWorker
import com.allat.mboychenko.silverthread.utils.updateVersion
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.*
import java.util.concurrent.TimeUnit

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
                    presentersModule,
                    exoPlayerStorageModule
                )
            )
        }

        FileLoaderService.commandRefreshLoadings(applicationContext)

        updateVersion(this)
        scheduleTimersCheckerWork()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun scheduleTimersCheckerWork() {
        val timersUpdateBuilder =
            PeriodicWorkRequest.Builder(
                UpdateTimersWorker::class.java,
                12,
                TimeUnit.HOURS,
                30,
                TimeUnit.MINUTES
            )

        val now = Calendar.getInstance()
        val nowHour = now.get(Calendar.HOUR_OF_DAY)
        val whenStartHour = if (nowHour in 5..16) 17 else 5
        val nextDay = whenStartHour == 5 && nowHour !in 0..4

        val fireIn = Calendar.getInstance()
        fireIn.set(Calendar.HOUR_OF_DAY, whenStartHour)
        fireIn.set(Calendar.MINUTE, 30)
        fireIn.set(Calendar.SECOND, 0)
        if (nextDay) {
            fireIn.add(Calendar.DAY_OF_MONTH, 1)
        }

        timersUpdateBuilder.setInitialDelay(fireIn.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(DAILY_TIMERS_CHECKER_WORK_TAG, ExistingPeriodicWorkPolicy.KEEP,  timersUpdateBuilder.build())
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopService(Intent(applicationContext, AllatRadioService::class.java)
            .apply { action = AllatRadioService.ACTION_STOP })
    }

    companion object {
        private const val DAILY_TIMERS_CHECKER_WORK_TAG = "DAILY_TIMERS_CHECKER_WORK_TAG"
    }

}