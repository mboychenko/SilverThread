package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.presentation.helpers.*

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val timezone = intent.getIntExtra(NOTIFICATION_TIMEZONE_EXTRAS, 3)
        val allatTimeZone = AllatTimeZone.values()[timezone]
        val allatAction = intent.action

        runTaskOnComputation {
            when (allatAction) {
                AlarmNotificationCodes.BEFORE.action ->
                    showNotificationAndReInit(context, AlarmNotificationCodes.BEFORE, allatTimeZone, intent.extras)
                AlarmNotificationCodes.BEFORE_UPDATE.action ->
                    showNotification(context, AlarmNotificationCodes.BEFORE_UPDATE, intent.extras)
                AlarmNotificationCodes.START.action ->
                    showNotificationAndReInit(context, AlarmNotificationCodes.START, allatTimeZone, intent.extras)
                AlarmNotificationCodes.END.action ->
                    showNotificationAndReInit(context, AlarmNotificationCodes.END, allatTimeZone, intent.extras)
                AlarmNotificationCodes.QUOTE.action -> TODO()
                AlarmNotificationCodes.CANCEL.action -> hideNotification(context, intent.extras)
            }
        }

    }

    private fun showNotificationAndReInit(context: Context, notificationCode: AlarmNotificationCodes,
                                          timeZone: AllatTimeZone, extras: Bundle?) {
        showNotification(context, notificationCode, extras)

        val alarmTime = when (notificationCode) {
            AlarmNotificationCodes.BEFORE -> {
                val millisOffset = extras?.getLong(NOTIFICATION_BEFORE_MILLIS_EXTRAS, -1) ?: -1
                if (millisOffset != -1L) {
                    AllatHelper.getMillisToAllatStart(timeZone, allatAfterNext = true) - millisOffset
                } else {
                    millisOffset
                }
            }
            AlarmNotificationCodes.START -> AllatHelper.getMillisToAllatStart(timeZone)
            AlarmNotificationCodes.END -> AllatHelper.getMillisToAllatEnd(timeZone)
            else -> -1
        }


        if (alarmTime != -1L) {
            setAlarm(context, alarmTime, notificationCode.action, notificationCode.ordinal, extras)
        }
    }
}