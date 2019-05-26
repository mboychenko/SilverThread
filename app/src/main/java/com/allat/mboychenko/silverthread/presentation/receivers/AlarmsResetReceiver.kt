package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allat.mboychenko.silverthread.presentation.helpers.AlarmNotificationCodes

class AlarmsResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingBroadcast = goAsync()
            intent.action = AlarmNotificationCodes.REINIT_TIMERS.action
            AlarmNotificationsExecutor(context)
                .apply { pendingResult =  pendingBroadcast }
                .onHandleNotification(intent)
        }
    }

}