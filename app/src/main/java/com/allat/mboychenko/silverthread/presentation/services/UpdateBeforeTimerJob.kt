package com.allat.mboychenko.silverthread.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneStorage
import com.allat.mboychenko.silverthread.presentation.helpers.*
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.TimeUnit

class UpdateBeforeTimerJob : JobIntentService() {

    private val storage: AllatTimeZoneStorage by inject()

    private var stopFlag = false

    override fun onHandleWork(intent: Intent) {
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(receiver, IntentFilter(NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS))

        val notifyBefore = storage.getAllatNotificationBeforeMins()
        val timezone = storage.getAllatTimezone()
        val oneSec = TimeUnit.SECONDS.toMillis(1)
        val oneMin = TimeUnit.MINUTES.toMillis(1)
        val tenMin = TimeUnit.MINUTES.toMillis(10)

        var sync = true
        val toAllat = AllatHelper.getMillisToAllatStart(timezone)
        val allatTime = toAllat + System.currentTimeMillis()

        val currentTimeCalendar = Calendar.getInstance()

        Log.d("UpdateBeforeTimerJob", String.format("beforeEnterIf %d %d", toAllat, tenMin))
        if (TimeUnit.MILLISECONDS.toMinutes(toAllat) <= notifyBefore && toAllat >= tenMin) {

            val toStart = 60 - currentTimeCalendar.get(Calendar.MINUTE)
            if (toStart < notifyBefore) {                                       //todo check example 13 < 15
                showNotification(exactMins = toStart)
                Log.d("UpdateBeforeTimerJob", "insideImmediatltly")
            }

//            if (TimeUnit.MILLISECONDS.toMinutes(toAllat) < notifyBefore) {
//                showNotification(allatTime)
//                Log.d("UpdateBeforeTimerJob", "insideImmediatltly")
//
//            }

            while (getRemainingWithSecondOffset(allatTime) >= tenMin && !stopFlag) {
                Log.d("UpdateBeforeTimerJob", "insideLoop")
                if (sync) {
                    while (Calendar.getInstance().get(Calendar.SECOND) != 0) {
                        Thread.sleep(oneSec)
                    }
                    sync = false
                } else {
                    Thread.sleep(oneMin)
                }

                showNotification(allatTime)

            }
        }

        hideAllatNotification(applicationContext)

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)

    }

    private fun showNotification(allatTime: Long = 0, exactMins: Int = 0) {
        showNotification(
            applicationContext,
            AlarmNotificationCodes.ALLAT_BEFORE_UPDATE,
            Bundle().apply {
                putLong(
                    NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS,
                    if (exactMins != 0) TimeUnit.MINUTES.toMillis(exactMins.toLong()) else getRemainingWithSecondOffset(allatTime)
                )
            })
    }

    private fun getRemainingWithSecondOffset(allatTime: Long) = allatTime - System.currentTimeMillis() + 1000

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
    }

    override fun onStopCurrentWork(): Boolean {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
        return true
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopFlag = true
            hideAllatNotification(applicationContext)
        }
    }

    companion object {
        fun startUpdateTimer(context: Context) {
            val intent = Intent()
            enqueueWork(context, UpdateBeforeTimerJob::class.java, 9856, intent)
        }

        fun stopUpdateTimer(context: Context) {
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS))
        }
    }

}

