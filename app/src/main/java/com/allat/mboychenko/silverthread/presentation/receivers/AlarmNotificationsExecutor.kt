package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.*
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.getPublicDownloadsStorageDir
import com.allat.mboychenko.silverthread.presentation.helpers.*
import java.util.concurrent.TimeUnit

class AlarmNotificationsExecutor(val context: Context) {

    var pendingResult: BroadcastReceiver.PendingResult? = null

    fun saveLogcatToFile() {
        val fileName = "/logcat_" + System.currentTimeMillis() + ".txt"
        val outputFile = getPublicDownloadsStorageDir("allat_logcat")
        val process = Runtime.getRuntime().exec("logcat NotificationTimer:D *:S -f " + outputFile?.absolutePath + fileName)
    }

    fun onHandleNotification(intent: Intent) {
        saveLogcatToFile()
        Log.d("NotificationTimer", "taking WL")

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager        //todo check on api 19
        val wl = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlarmNotificationsExecutor:WakeLock")
        wl.acquire(TimeUnit.SECONDS.toMillis(15))

        runTaskOnComputation(
            {
                val storage = StorageImplementation(context)
                val allatStorage: AllatTimeZoneStorage = AllatTimeZoneInteractor(storage)
                val quotesStorage: QuotesStorage = QuotesInteractor(storage)

                val bundle = intent.extras?.let { Bundle(it) }

                Log.d("NotificationTimer", "onHandleNotification ${intent.action}")

                when (intent.action) {
                    AlarmNotificationCodes.REINIT_TIMERS.action -> {
                        reInitTimers(
                            context,
                            allatStorage.getAllatTimezone(),
                            allatStorage.getAllatNotificationBeforeMins(),
                            allatStorage.getAllatNotificationStart(),
                            allatStorage.getAllatNotificationEnd()
                        )
                    }
                    AlarmNotificationCodes.ALLAT_BEFORE.action ->
                        showNotificationAndReInit(context, AlarmNotificationCodes.ALLAT_BEFORE, bundle)
                    AlarmNotificationCodes.ALLAT_BEFORE_UPDATE.action ->
                        showNotification(context, AlarmNotificationCodes.ALLAT_BEFORE_UPDATE, bundle)
                    AlarmNotificationCodes.CANCEL_ALLAT_UPDATE.action ->
                        removeAlarm(
                            context,
                            AlarmNotificationCodes.ALLAT_BEFORE_UPDATE.action,
                            AlarmNotificationCodes.ALLAT_BEFORE_UPDATE.ordinal
                        )
                    AlarmNotificationCodes.ALLAT_START.action ->
                        showNotificationAndReInit(context, AlarmNotificationCodes.ALLAT_START, bundle)
                    AlarmNotificationCodes.ALLAT_END.action ->
                        showNotificationAndReInit(context, AlarmNotificationCodes.ALLAT_END, bundle)
                    AlarmNotificationCodes.QUOTE.action -> TODO()
                    AlarmNotificationCodes.CANCEL.action -> hideNotification(context, bundle)
                }
            },
            {
                wl.release()
                pendingResult?.finish()
                Log.d("NotificationTimer", "Release WL")
            })
    }

    private fun showNotificationAndReInit(context: Context, notificationCode: AlarmNotificationCodes, extras: Bundle?) {
        showNotification(context, notificationCode, extras)
        setAlarm(context,                                       //reInit all timers if need in 2 hrs
            TimeUnit.HOURS.toMillis(2),
            AlarmNotificationCodes.REINIT_TIMERS.action,
            AlarmNotificationCodes.REINIT_TIMERS.ordinal)
    }

}