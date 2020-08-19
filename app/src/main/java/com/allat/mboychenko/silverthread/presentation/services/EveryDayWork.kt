package com.allat.mboychenko.silverthread.presentation.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.allat.mboychenko.silverthread.domain.interactor.AllatNotificationsSettingsStorage
import com.allat.mboychenko.silverthread.domain.interactor.AppSettingsStorage
import com.allat.mboychenko.silverthread.domain.interactor.AppSettingsStorageInteractor
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.isWorkScheduled
import com.allat.mboychenko.silverthread.presentation.helpers.reInitTimers
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnComputation
import com.allat.mboychenko.silverthread.presentation.helpers.setupRandomQuoteNextAlarm
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit


class EveryDayWork(val context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val settingsStorage: AppSettingsStorage by inject()
        val allatStorage: AllatNotificationsSettingsStorage by inject()
        val quoteStorage: QuotesDetailsStorage by inject()
        val workManager: WorkManager by inject()

        settingsStorage.addOneEveryDayWork()

        reInitTimers(context,
            allatStorage.getAllatTimezone(),
            allatStorage.getAllatNotificationBeforeMins(),
            allatStorage.getAllatNotificationStart(),
            allatStorage.getAllatNotificationEnd(),
            false)

        val possibleQuotesPerDay = quoteStorage.getRandomQuotesTimesInDay()
        val scheduledQuoteTime = quoteStorage.getNextQuoteTime()

        Log.d("LogQuotes", "UpdateTimersWorker $possibleQuotesPerDay $scheduledQuoteTime")

        if (possibleQuotesPerDay > 0 && scheduledQuoteTime < Calendar.getInstance().timeInMillis) {
            Log.d("LogQuotes", "[UpdateTimersWorker]RESCHEDULE")
            setupRandomQuoteNextAlarm(context)
        }

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) <= 12) {
            try {
                BackupWork.enqueueWork(workManager, BackupWork.Companion.Action.BACKUP_AUTO)
            } catch (e: Throwable) {
                Log.d("BackupWork", "Finished with error, its ok")
            }
        }

        return Result.success()
    }

    companion object {
        private const val EVERY_DAY_WORK_TASK_TAG = "EVERY_DAY_WORK_TASK_TAG"

        fun initWork(workManager: WorkManager, context: Context) {
            runTaskOnComputation {
                val storage = AppSettingsStorageInteractor(context) as AppSettingsStorage
                val everyDayWorkExecutedTimes = storage.getEveryDayWorkTimes()
                if (everyDayWorkExecutedTimes > 9 || !workManager.isWorkScheduled(EVERY_DAY_WORK_TASK_TAG)) {

                    storage.clearEveryDayWork()

                    val timersUpdateBuilder =
                        PeriodicWorkRequest.Builder(
                            EveryDayWork::class.java,
                            12,
                            TimeUnit.HOURS
                        )

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

                    val initDelay = fireIn.timeInMillis - now.timeInMillis

                    timersUpdateBuilder.setInitialDelay(initDelay, TimeUnit.MILLISECONDS)

                    workManager.enqueueUniquePeriodicWork(
                        EVERY_DAY_WORK_TASK_TAG,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        timersUpdateBuilder.build()
                    )
                }
            }
        }
    }
}

