package com.allat.mboychenko.silverthread.presentation.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.presentation.receivers.TimerExpiredReceiver
import java.util.*
import java.util.concurrent.TimeUnit

fun setAlarm(context: Context, millisRemaining: Long, intentAction: String, requestCode: Int, extras: Bundle? = null) {
    val wakeUpTime = nowMillis + millisRemaining
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val pendingIntent = createAlarmPendingIntent(context, intentAction, requestCode, extras)

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ->
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(wakeUpTime, pendingIntent), pendingIntent)
        else -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
    }
}

fun removeAlarm(context: Context, intentAction: String, requestCode: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(context, intentAction, requestCode)
}

private fun AlarmManager.cancel(context: Context, intentAction: String, requestCode: Int) {
    val pendingIntent = createAlarmPendingIntent(context, intentAction, requestCode)
    this.cancel(pendingIntent)
}

private fun createAlarmPendingIntent(context: Context, intentAction: String,
                                     requestCode: Int, extras: Bundle? = null): PendingIntent {
    val intent = Intent(context, TimerExpiredReceiver::class.java)
    intent.action = intentAction
    extras?.let { intent.putExtras(it) }

    return PendingIntent.getBroadcast(context, requestCode, intent, FLAG_CANCEL_CURRENT)
}

fun setupAllatBeforeAlarm(context: Context, minsBefore: Int, allatTimezone: AllatTimeZone) {
    val millisOffset = TimeUnit.MINUTES.toMillis(minsBefore.toLong())
    var millisRemaining = AllatHelper.getMillisToAllatStart(allatTimezone) - millisOffset

    if (millisRemaining <= 0) {
        millisRemaining = AllatHelper.getMillisToAllatStart(allatTimezone, true) - millisOffset
    }

    val bundle = Bundle().apply {
        putLong(NOTIFICATION_BEFORE_MILLIS_EXTRAS, millisOffset)
    }

    setAlarm(context, millisRemaining, AlarmNotificationCodes.ALLAT_BEFORE.action, AlarmNotificationCodes.ALLAT_BEFORE.ordinal, bundle)
}

fun setupAllatStartAlarm(context: Context, allatTimezone: AllatTimeZone) {
    setAlarm(context, AllatHelper.getMillisToAllatStart(allatTimezone),
        AlarmNotificationCodes.ALLAT_START.action, AlarmNotificationCodes.ALLAT_START.ordinal)
}

fun setupAllatEndAlarm(context: Context, allatTimezone: AllatTimeZone) {
    setAlarm(context, AllatHelper.getMillisToAllatEnd(allatTimezone),
        AlarmNotificationCodes.ALLAT_END.action, AlarmNotificationCodes.ALLAT_END.ordinal)
}

fun reInitTimers(context: Context, allatTimezone: AllatTimeZone, allatRemindBeforeMins: Int,
                 allatNotificationStart: Boolean, allatNotificationEnd: Boolean) {
    if (allatTimezone != AllatTimeZone.NOT_INIT) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (code in AlarmNotificationCodes.values()) {
            alarmManager.cancel(context, code.action, code.ordinal)
        }

        if (allatRemindBeforeMins > 0) {
            setupAllatBeforeAlarm(context, allatRemindBeforeMins, allatTimezone)
        }

        if (allatNotificationStart) {
            setupAllatStartAlarm(context, allatTimezone)
        }

        if (allatNotificationEnd) {
            setupAllatEndAlarm(context, allatTimezone)
        }
    }
    //todo quotesStorage notifications reinit
}

private val nowMillis: Long
    get() = Calendar.getInstance().timeInMillis

enum class AlarmNotificationCodes(val action: String) {
    ALLAT_BEFORE("ALLAT_BEFORE"),
    ALLAT_BEFORE_UPDATE("ALLAT_BEFORE_UPDATE"),
    CANCEL_ALLAT_UPDATE("CANCEL_ALLAT_UPDATE"),
    ALLAT_START("ALLAT_START_NOTIFICATION"),
    ALLAT_END("ALLAT_END_NOTIFICATION"),
    QUOTE("NOTIFICATION_QUOTE"),
    CANCEL("CANCEL"),
    REINIT_TIMERS("REINIT_TIMERS")
}

const val NOTIFICATION_BEFORE_MILLIS_EXTRAS = "NOTIFICATION_BEFORE_MILLIS_EXTRAS"
const val NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS = "NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS"