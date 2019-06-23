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
    private var skipNotifFlag = false

    override fun onHandleWork(intent: Intent) {
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(receiver, IntentFilter(NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS))

        setInterruptIfStopped(true)

        val notifyBefore = storage.getAllatNotificationBeforeMins()
        val timezone = storage.getAllatTimezone()
        val oneSec = TimeUnit.SECONDS.toMillis(1)
        val oneMin = TimeUnit.MINUTES.toMillis(1)
        val tenMin = TimeUnit.MINUTES.toMillis(10)

        var sync = true
        val toAllat = AllatHelper.getMillisToAllatStart(timezone)
        val allatTime = toAllat + System.currentTimeMillis()

        Log.d("UpdateBeforeTimerJob", String.format("beforeEnterIf %d %d", toAllat, allatTime))
        if (TimeUnit.MILLISECONDS.toMinutes(toAllat) <= notifyBefore && toAllat >= tenMin) {

            val toStart = getMinsRemaning()
            if (toStart < notifyBefore) {
                showNotification(toStart)
                Log.d("UpdateBeforeTimerJob", String.format("insideImmediatltly %d", toStart))
            }


            while (getMinsRemaning() >= 10 && !stopFlag && !isStopped) {
                Log.d("UpdateBeforeTimerJob", String.format("insideLoop %b", sync))

                if (sync) {
                    Log.d("UpdateBeforeTimerJob", "Going sync")
                    while (Calendar.getInstance().get(Calendar.SECOND) != 0) {
                        Thread.sleep(oneSec)
                    }
                    sync = false
                } else {
                    Log.d("UpdateBeforeTimerJob", "Going sleep 60sec")
                    Thread.sleep(oneMin)
                }

                Log.d("UpdateBeforeTimerJob", String.format("showNotif from thread %s", Thread.currentThread().name))
                if (!skipNotifFlag && isBeforeAllatStart(notifyBefore, toAllat, allatTime))
                    showNotification(getMinsRemaning())

            }
        }

        if (!skipNotifFlag && isBeforeAllatStart(notifyBefore, toAllat, allatTime)) {
            hideAllatNotification(applicationContext)
        }

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)

    }

    private fun getMinsRemaning() = 60 - Calendar.getInstance().get(Calendar.MINUTE)

    private fun isBeforeAllatStart(notifyBefore: Int, toAllat: Long, allatTime: Long) =
                TimeUnit.MILLISECONDS.toMinutes(toAllat) <= notifyBefore && allatTime > System.currentTimeMillis()

    private fun showNotification(exactMins: Int) {
        showNotification(
            applicationContext,
            AlarmNotificationCodes.ALLAT_BEFORE_UPDATE,
            Bundle().apply {
                putLong(
                    NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS,
                    TimeUnit.MINUTES.toMillis(exactMins.toLong())
                )
            })
    }

    override fun onStopCurrentWork(): Boolean {
        Log.d("UpdateBeforeTimerJob", "onStopCurrentWork")
        handleStop()
        return true
    }

    override fun onDestroy() {
        Log.d("UpdateBeforeTimerJob", "Destroy")
        handleStop()
    }

    private fun handleStop() {
        stopFlag = true
        skipNotifFlag = true
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopFlag = true
            skipNotifFlag = true
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

