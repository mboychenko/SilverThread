package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allat.mboychenko.silverthread.presentation.helpers.*

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action){
            NotificationConstants.ACTION_STOP -> {
//                removeAlarm(context)
//                PrefUtil.setTimerState(TimerActivity.TimerState.Stopped, context)
                hideRadioNotification(context)
            }
            NotificationConstants.ACTION_PAUSE -> {
//                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
//                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
//                val nowSeconds = TimerActivity.nowSeconds
//
//                secondsRemaining -= nowSeconds - alarmSetTime
//                PrefUtil.setSecondsRemaining(secondsRemaining, context)

//                removeAlarm(context)
//                PrefUtil.setTimerState(TimerActivity.TimerState.Paused, context)
                showTimerPaused(context)
            }
        }
    }
}