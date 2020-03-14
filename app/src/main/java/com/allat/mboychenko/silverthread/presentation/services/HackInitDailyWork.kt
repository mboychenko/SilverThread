package com.allat.mboychenko.silverthread.presentation.services

import android.content.Context
import androidx.work.*
import com.allat.mboychenko.silverthread.presentation.helpers.isWorkScheduled
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit

class HackInitDailyWork(val context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val workManager: WorkManager by inject()
        EveryDayWork.initUniquePeriodicWork(workManager)
        return Result.success()
    }

    companion object {

        private const val HACK_INIT_DAILY_WORK_TAG = "HACK_INIT_DAILY_WORK_TAG"

        fun initWork(workManager: WorkManager, replace: Boolean = false) {
            if (replace || !workManager.isWorkScheduled(HACK_INIT_DAILY_WORK_TAG)) {

                val now = Calendar.getInstance()
                val nowHour = now.get(Calendar.HOUR_OF_DAY)
                val whenStartHour = if (nowHour in 5..16) 17 else 5
                val nextDay = whenStartHour == 5 && nowHour !in 0..4

                val fireIn = Calendar.getInstance()
                fireIn.set(Calendar.HOUR_OF_DAY, whenStartHour)
                fireIn.set(Calendar.MINUTE, 25)
                fireIn.set(Calendar.SECOND, 0)
                if (nextDay) {
                    fireIn.add(Calendar.DAY_OF_MONTH, 1)
                }

                val delay = fireIn.timeInMillis - now.timeInMillis

                val work =
                    OneTimeWorkRequest
                        .Builder(HackInitDailyWork::class.java)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build()

                workManager.enqueueUniqueWork(
                    HACK_INIT_DAILY_WORK_TAG,
                    ExistingWorkPolicy.REPLACE,
                    work
                )
            }
        }
    }
}