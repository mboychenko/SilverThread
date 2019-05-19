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

fun setAlarm(context: Context, millisRemaining: Long, intentAction: String, requestCode: Int, extras: Bundle?) {
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
    val pendingIntent = createAlarmPendingIntent(context, intentAction, requestCode)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

private fun createAlarmPendingIntent(context: Context, intentAction: String,
                                     requestCode: Int, extras: Bundle? = null): PendingIntent {
    val intent = Intent(context, TimerExpiredReceiver::class.java)
    intent.action = intentAction
    extras?.let { intent.putExtras(it) }

    return PendingIntent.getBroadcast(context, requestCode, intent, FLAG_CANCEL_CURRENT)
}

fun setupBeforeAlarm(context: Context, minsBefore: Int, allatTimezone: AllatTimeZone) {
    val millisOffset = TimeUnit.MINUTES.toMillis(minsBefore.toLong())
    var millisRemaining = AllatHelper.getMillisToAllatStart(allatTimezone) - millisOffset

    if (millisRemaining <= 0) {
        millisRemaining = AllatHelper.getMillisToAllatStart(allatTimezone, true) - millisOffset
    }

    val bundle = Bundle().apply {
        putLong(NOTIFICATION_BEFORE_MILLIS_EXTRAS, millisOffset)
        putInt(NOTIFICATION_TIMEZONE_EXTRAS, allatTimezone.ordinal)
    }

    setAlarm(context, millisRemaining, AlarmNotificationCodes.BEFORE.action, AlarmNotificationCodes.BEFORE.ordinal, bundle)
}

fun setupStartAlarm(context: Context, allatTimezone: AllatTimeZone) {
    setAlarm(context, AllatHelper.getMillisToAllatStart(allatTimezone),
        AlarmNotificationCodes.START.action, AlarmNotificationCodes.START.ordinal,
        Bundle().apply { putInt(NOTIFICATION_TIMEZONE_EXTRAS, allatTimezone.ordinal) })
}

fun setupEndAlarm(context: Context, allatTimezone: AllatTimeZone) {
    setAlarm(context, AllatHelper.getMillisToAllatEnd(allatTimezone),
        AlarmNotificationCodes.END.action, AlarmNotificationCodes.END.ordinal,
        Bundle().apply { putInt(NOTIFICATION_TIMEZONE_EXTRAS, allatTimezone.ordinal) })
}

fun reInitTimers(context: Context, allatTimezone: AllatTimeZone, allatRemindBeforeMins: Int,
                 allatNotificationStart: Boolean, allatNotificationEnd: Boolean) {
    if (allatTimezone != AllatTimeZone.NOT_INIT) {

        removeAlarm(context, AlarmNotificationCodes.BEFORE_UPDATE.action, AlarmNotificationCodes.BEFORE_UPDATE.ordinal)
        removeAlarm(context, AlarmNotificationCodes.CANCEL.action, AlarmNotificationCodes.CANCEL.ordinal)

        if (allatRemindBeforeMins != -1) {
            setupBeforeAlarm(context, allatRemindBeforeMins, allatTimezone)
        }

        if (allatNotificationStart) {
            setupStartAlarm(context, allatTimezone)
        }

        if (allatNotificationEnd) {
            setupEndAlarm(context, allatTimezone)
        }
    }
}

private val nowMillis: Long
    get() = Calendar.getInstance().timeInMillis

enum class AlarmNotificationCodes(val action: String) {
    BEFORE("NOTIFICATION_BEFORE"),
    BEFORE_UPDATE("BEFORE_UPDATE"),
    START("NOTIFICATION_START"),
    END("NOTIFICATION_END"),
    QUOTE("NOTIFICATION_QUOTE"),
    CANCEL("CANCEL")
}

const val NOTIFICATION_BEFORE_MILLIS_EXTRAS = "NOTIFICATION_BEFORE_MILLIS_EXTRAS"
const val NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS = "NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS"
const val NOTIFICATION_TIMEZONE_EXTRAS = "NOTIFICATION_TIMEZONE_EXTRAS"
