package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingBroadcast = goAsync()
        AlarmNotificationsExecutor(context)
            .apply { pendingResult =  pendingBroadcast }
            .onHandleNotification(intent)
    }
}