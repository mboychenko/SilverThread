package com.allat.mboychenko.silverthread.presentation.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.allat.mboychenko.silverthread.domain.interactor.AllatNotificationsSettingsStorage
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.reInitTimers
import com.allat.mboychenko.silverthread.presentation.helpers.setupRandomQuoteNextAlarm
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit


class EveryDayWork(val context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val allatStorage: AllatNotificationsSettingsStorage by inject()
        val quoteStorage: QuotesDetailsStorage by inject()
        val workManager: WorkManager by inject()

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
        const val DAILY_TIMERS_CHECKER_WORK_TAG = "DAILY_TIMERS_CHECKER_WORK_TAG"

        fun initUniquePeriodicWork(workManager: WorkManager) {
            val timersUpdateBuilder =
                PeriodicWorkRequest.Builder(
                    EveryDayWork::class.java,
                    12,
                    TimeUnit.HOURS
                )

            //hack
            timersUpdateBuilder.setInitialDelay(-1L, TimeUnit.MILLISECONDS)

            workManager.enqueueUniquePeriodicWork(
                DAILY_TIMERS_CHECKER_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                timersUpdateBuilder.build()
            )
        }
    }
}

