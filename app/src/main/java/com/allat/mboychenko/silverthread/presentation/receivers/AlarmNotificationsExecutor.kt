package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import com.allat.mboychenko.silverthread.presentation.helpers.setupRandomQuoteNextAlarm
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.domain.interactor.*
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.services.UpdateBeforeTimerJob
import java.util.concurrent.TimeUnit

class AlarmNotificationsExecutor(val context: Context) {

    var pendingResult: BroadcastReceiver.PendingResult? = null

    fun onHandleNotification(intent: Intent) {

        Log.d("AlarmNotificationsExec", "taking WL")

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlarmNotificationsExecutor:WakeLock")
        wl.acquire(TimeUnit.SECONDS.toMillis(15))

        runTaskOnComputation(
            {
                val storage = StorageImplementation(context)
                val allatStorage: AllatTimeZoneStorage = AllatTimeZoneInteractor(storage)

                val bundle = Bundle().apply {
                    putBoolean(ALLAT_START_ARG, allatStorage.getAllatNotificationStart())
                    putBoolean(ALLAT_END_ARG, allatStorage.getAllatNotificationEnd())
                    if (intent.extras != null) {
                        putAll(intent.extras)
                    }
                }

                Log.d("AlarmNotificationsExec", "onHandleNotification ${intent.action}")

                when (intent.action) {
                    AlarmNotificationCodes.REINIT_TIMERS.action -> {
                        reInit(context, allatStorage, false)
                    }
                    AlarmNotificationCodes.REINIT_TIMERS_ENV.action -> {
                        reInit(context, allatStorage, true)
                    }
                    AlarmNotificationCodes.ALLAT_BEFORE.action -> {
                        UpdateBeforeTimerJob.startUpdateTimer(context)
                        showNotificationAndReInit(context, AlarmNotificationCodes.ALLAT_BEFORE, bundle)
                    }
                    AlarmNotificationCodes.CANCEL_ALLAT_UPDATE.action -> {
                        UpdateBeforeTimerJob.stopUpdateTimer(context)
                        hideAllatNotification(context)
                    }
                    AlarmNotificationCodes.ALLAT_START.action ->
                        showNotificationAndReInit(context, AlarmNotificationCodes.ALLAT_START, bundle)
                    AlarmNotificationCodes.ALLAT_END.action ->
                        showNotificationAndReInit(context, AlarmNotificationCodes.ALLAT_END, bundle)
                    AlarmNotificationCodes.QUOTE.action -> {
                        showNotification(context, AlarmNotificationCodes.QUOTE, bundle)
                        setupRandomQuoteNextAlarm(context, true)
                    }
                    AlarmNotificationCodes.CANCEL.action -> hideNotification(context, bundle)
                }
            },
            {
                try {
                    wl.release()
                } catch (e: Throwable) {
                    //catch known issue for specific Android versions, nothing to do
                }

                pendingResult?.finish()
                Log.d("AlarmNotificationsExec", "Release WL")
            })
    }

    private fun reInit(context: Context, allatStorage: AllatTimeZoneStorage, reInitQuotes: Boolean) {
        reInitTimers(context,
            allatStorage.getAllatTimezone(),
            allatStorage.getAllatNotificationBeforeMins(),
            allatStorage.getAllatNotificationStart(),
            allatStorage.getAllatNotificationEnd(),
            reInitQuotes)
    }

    private fun showNotificationAndReInit(context: Context, notificationCode: AlarmNotificationCodes, extras: Bundle?) {
        showNotification(context, notificationCode, extras)
        setAlarmRemainingTime(context,                                       //reInit all timers if need in 6  hrs
            TimeUnit.HOURS.toMillis(6),
            AlarmNotificationCodes.REINIT_TIMERS.action,
            AlarmNotificationCodes.REINIT_TIMERS.code)
    }

}