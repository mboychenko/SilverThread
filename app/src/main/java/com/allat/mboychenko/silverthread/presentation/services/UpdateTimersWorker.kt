package com.allat.mboychenko.silverthread.presentation.services

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.allat.mboychenko.silverthread.domain.interactor.AllatNotificationsSettingsStorage
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.reInitTimers
import com.allat.mboychenko.silverthread.presentation.helpers.setupRandomQuoteNextAlarm
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class UpdateTimersWorker(val context: Context, params: WorkerParameters)
    : Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        val allatStorage: AllatNotificationsSettingsStorage by inject()
        val quoteStorage: QuotesDetailsStorage by inject()

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

        return Result.success()
    }
}