package com.allat.mboychenko.silverthread.presentation.helpers

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.receivers.TimerRadioNotificationActionReceiver
import com.allat.mboychenko.silverthread.presentation.views.activities.MainActivity
import java.text.SimpleDateFormat
import android.media.AudioAttributes
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import com.allat.mboychenko.silverthread.presentation.receivers.TimerExpiredReceiver
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID_ALLAT = "allat_notif"
private const val CHANNEL_NAME_ALLAT = "Allat Notifications"

private const val CHANNEL_ID_ALLAT_SILENCE = "allat_notif_silence"
private const val CHANNEL_NAME_ALLAT_SILENCED = "Allat Notifications Muted"

private const val CHANNEL_ID_QUOTES = "quotes_notif"
private const val CHANNEL_NAME_QUOTES = "Quotes Notifications"

private const val NOTIFICATION_ACTION_ALLAT = "AllatNotification"
private const val NOTIFICATION_ACTION_BEFORE_UPDATE = "AllatNotificationBeforeUpdate"
private const val NOTIFICATION_ACTION_QUOTE = "QuoteNotification"
private const val NOTIFICATION_CANCEL_ID_EXTRA = "NOTIFICATION_CANCEL_ID_EXTRA"
private const val NOTIFICATION_ID_ALLAT = 0
private const val NOTIFICATION_ID_QUOTE = 1
private const val NOTIFICATION_ID_RADIO = 2
private const val NOTIFICATION_ID_ALLAT_BEFORE_UPDATE = 3

fun showNotification(context: Context, notificationCode: AlarmNotificationCodes, extras: Bundle?) {
    //setup
    var remindBefore: Long = 0
    var remindBeforeUpdate: Long = 0

    extras?.let {
        remindBefore = extras.getLong(NOTIFICATION_BEFORE_MILLIS_EXTRAS)
        remindBeforeUpdate = extras.getLong(NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS, remindBefore)
    }

    val title: String
    val text: String
    val action: String
    val notifChannelId: String
    val notifId: Int
    var playSound = true

    when (notificationCode) {
        AlarmNotificationCodes.START -> {
            title = context.getString(R.string.allat_reminder)
            text = context.getString(R.string.allat_has_started)
            action = NOTIFICATION_ACTION_ALLAT
            notifId = NOTIFICATION_ID_ALLAT
            notifChannelId = CHANNEL_ID_ALLAT
        }
        AlarmNotificationCodes.END -> {
            title = context.getString(R.string.allat_reminder)
            text = context.getString(R.string.allat_has_ended)
            action = NOTIFICATION_ACTION_ALLAT
            notifId = NOTIFICATION_ID_ALLAT
            notifChannelId = CHANNEL_ID_ALLAT
        }
        AlarmNotificationCodes.BEFORE -> {
            title = context.getString(R.string.allat_reminder)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remindBefore).toInt()
            text = context.resources.getQuantityString(R.plurals.mins_to_allat, minutes, minutes)
            action = NOTIFICATION_ACTION_ALLAT
            notifId = NOTIFICATION_ID_ALLAT
            notifChannelId = CHANNEL_ID_ALLAT
        }
        AlarmNotificationCodes.BEFORE_UPDATE -> {
            title = context.getString(R.string.allat_reminder)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remindBeforeUpdate).toInt()
            text = context.resources.getQuantityString(R.plurals.mins_to_allat, minutes, minutes)
            action = NOTIFICATION_ACTION_BEFORE_UPDATE
            notifId = NOTIFICATION_ID_ALLAT //or NOTIFICATION_ID_ALLAT_BEFORE_UPDATE
            notifChannelId = CHANNEL_ID_ALLAT_SILENCE
            playSound = false
        }
        AlarmNotificationCodes.QUOTE -> {
            title = context.getString(R.string.random_quote)
            text = "todo quote"
            action = NOTIFICATION_ACTION_QUOTE
            notifId = NOTIFICATION_ID_QUOTE
            notifChannelId = CHANNEL_ID_QUOTES
        }
        else -> return
    }
    Log.d("NotificationTimer", "timerAfterInit $title, $text, $action, $notifId, $notifChannelId")
    //notify
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val nBuilder = getBasicNotificationBuilder(context, notifChannelId, playSound)
    nBuilder.setContentTitle(title)
        .setContentText(text)
        .setContentIntent(getPendingIntent(context, action, MainActivity::class.java))
        .setVibrate(longArrayOf(500, 500, 500, 500))
        .priority = if (playSound) IMPORTANCE_HIGH else IMPORTANCE_LOW

    if (notificationCode == AlarmNotificationCodes.BEFORE ||
        notificationCode == AlarmNotificationCodes.BEFORE_UPDATE) {
        nBuilder.setDeleteIntent(getRemoveBeforeUpdatesIntent(context))
        Log.d("NotificationTimer", "timerSetDeleteIntent RemindBefore $remindBefore")
        Log.d("NotificationTimer", "timerSetDeleteIntent RemindBeforeUpdate $remindBeforeUpdate")
    }

    val notification = nBuilder.build()
    nManager.createNotificationChannel(context, notification, playSound)
    nManager.notify(notifId, notification)

    //cancel action
    if (notificationCode == AlarmNotificationCodes.START ||
        notificationCode == AlarmNotificationCodes.END) {
        setAlarm(context, TimeUnit.MINUTES.toMillis(11),
            AlarmNotificationCodes.CANCEL.action,
            AlarmNotificationCodes.CANCEL.ordinal,
            Bundle().apply { putInt(NOTIFICATION_CANCEL_ID_EXTRA, notifId) })
    }

    //update & cancel action
    if (notificationCode == AlarmNotificationCodes.BEFORE ||
        notificationCode == AlarmNotificationCodes.BEFORE_UPDATE) {
        Log.d("NotificationTimer", "timerBeforeEnter $remindBefore : $remindBeforeUpdate")
        val oneMin = TimeUnit.MINUTES.toMillis(1)
        if (remindBeforeUpdate - oneMin > TimeUnit.MINUTES.toMillis(5)) {
            Log.d("NotificationTimer", "timerBeforeInside " + (remindBeforeUpdate - oneMin).toString())
            setAlarm(context, oneMin,
                AlarmNotificationCodes.BEFORE_UPDATE.action,
                AlarmNotificationCodes.BEFORE_UPDATE.ordinal,
                Bundle().apply {
                    putLong(
                        NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS,
                        remindBeforeUpdate - oneMin
                    )
                })
        } else {
            Log.d("NotificationTimer", "timerBeforeCancel " + (remindBeforeUpdate - oneMin).toString())
            setAlarm(context, oneMin,
                AlarmNotificationCodes.CANCEL.action,
                AlarmNotificationCodes.CANCEL.ordinal,
                Bundle().apply { putInt(NOTIFICATION_CANCEL_ID_EXTRA, notifId) })
        }
    }
}

fun showTimerRunning(context: Context, wakeUpTime: Long) {  //ongoing
    val stopIntent = Intent(context, TimerRadioNotificationActionReceiver::class.java)
//    stopIntent.action = NotificationConstants.ACTION_STOP
    val stopPendingIntent = PendingIntent.getBroadcast(
        context,
        0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    val pauseIntent = Intent(context, TimerRadioNotificationActionReceiver::class.java)
//    pauseIntent.action = NotificationConstants.ACTION_PAUSE
    val pausePendingIntent = PendingIntent.getBroadcast(
        context,
        0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

//    val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
//    nBuilder.setContentTitle("Timer is Running.")
//        .setContentText("End: ${df.format(Date(wakeUpTime))}")
//        .setContentIntent(getPendingIntent(context, "radio running", MainActivity::class.java))
//        .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
//        .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)

    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    nManager.createNotificationChannel(context, CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

//    nManager.notify(NOTIFICATION_ID_RADIO, nBuilder.build())
}

fun showTimerPaused(context: Context) {
    val resumeIntent = Intent(context, TimerRadioNotificationActionReceiver::class.java)
//    resumeIntent.action = NotificationConstants.ACTION_RESUME
    val resumePendingIntent = PendingIntent.getBroadcast(
        context,
        0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

//    val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
//    nBuilder.setContentTitle("Timer is paused.")
//        .setContentText("Resume?")
//        .setContentIntent(getPendingIntent(context,"radio paused", MainActivity::class.java))
//        .addAction(R.drawable.ic_play_arrow, "Resume", resumePendingIntent)

    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    nManager.createNotificationChannel(context, CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

//    nManager.notify(NOTIFICATION_ID_RADIO, nBuilder.build())
}

fun hideRadioNotification(context: Context) {
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nManager.cancel(NOTIFICATION_ID_RADIO)
}

fun hideNotification(context: Context, extras: Bundle?) {
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val cancelId = extras?.getInt(NOTIFICATION_CANCEL_ID_EXTRA, -1) ?: -1
    if (cancelId != -1) {
        nManager.cancel(cancelId)
    }
}

private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean)
        : NotificationCompat.Builder {
    val notificationSound: Uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.ring)
    val nBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.allatra)
        .setLights(Color.BLUE, 500, 3000)
        .setAutoCancel(true)
    if (playSound) nBuilder.setSound(notificationSound)
    return nBuilder
}

private fun <T> getPendingIntent(context: Context, action: String, javaClass: Class<T>): PendingIntent? {
    val resultIntent = Intent(context, javaClass)
    resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    resultIntent.action = action

    return PendingIntent.getActivity(
        context,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}

private fun getRemoveBeforeUpdatesIntent(context: Context): PendingIntent? {
    val resultIntent = Intent(context, TimerExpiredReceiver::class.java)
    resultIntent.action = AlarmNotificationCodes.CANCEL_UPDATE.action

    return PendingIntent.getBroadcast(
        context,
        AlarmNotificationCodes.CANCEL_UPDATE.ordinal,
        resultIntent,
        PendingIntent.FLAG_CANCEL_CURRENT
    )
}

@TargetApi(26)
private fun NotificationManager.createNotificationChannel(
    context: Context,
    notification: Notification,
    playSound: Boolean
) {
    val channelID = notification.channelId
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.getNotificationChannel(channelID) == null) {
        val channelName = when (channelID) {
            CHANNEL_ID_ALLAT -> CHANNEL_NAME_ALLAT
            CHANNEL_ID_ALLAT_SILENCE -> CHANNEL_NAME_ALLAT_SILENCED
            CHANNEL_ID_QUOTES -> CHANNEL_NAME_QUOTES
            else -> "DefaultChannel"
        }

        val channelImportance = if (playSound) NotificationManager.IMPORTANCE_HIGH
        else NotificationManager.IMPORTANCE_LOW //IMPORTANCE_DEFAULT?
        val nChannel = NotificationChannel(channelID, channelName, channelImportance)
        nChannel.enableLights(true)
        nChannel.lightColor = Color.BLUE

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        nChannel.vibrationPattern = longArrayOf(500, 500, 500, 500)
        nChannel.setSound(
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.ring),
            audioAttributes
        )
        this.createNotificationChannel(nChannel)
    }
}
