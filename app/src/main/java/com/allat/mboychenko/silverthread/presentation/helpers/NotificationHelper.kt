package com.allat.mboychenko.silverthread.presentation.helpers

import android.annotation.TargetApi
import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.activities.MainActivity
import android.media.AudioAttributes
import android.os.Bundle
import android.util.Log
import com.allat.mboychenko.silverthread.presentation.receivers.TimerExpiredReceiver
import java.util.concurrent.TimeUnit
import android.app.PendingIntent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import com.allat.mboychenko.silverthread.presentation.services.AllatRadioService
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.session.MediaButtonReceiver

private const val CHANNEL_ID_ALLAT = "allat_notif"
private const val CHANNEL_NAME_ALLAT = "Allat Notifications"

private const val CHANNEL_NAME_ALLAT_SILENCED = "Allat Notifications Muted"
private const val CHANNEL_NAME_QUOTES = "Quotes Notifications"
private const val CHANNEL_NAME_RADIO = "Radio Notifications"

const val CHANNEL_ID_ALLAT_SILENCE = "allat_notif_silence"
const val CHANNEL_ID_QUOTES = "quotes_notif"
const val CHANNEL_ID_RADIO = "radio_notif"

const val NOTIFICATION_ACTION_ALLAT = "AllatNotification"
const val NOTIFICATION_ACTION_BEFORE_UPDATE = "AllatNotificationBeforeUpdate"
const val NOTIFICATION_ACTION_QUOTE = "QuoteNotification"
const val NOTIFICATION_ACTION_RADIO = "RadioNotification"

const val NOTIFICATION_CANCEL_ID_EXTRA = "NOTIFICATION_CANCEL_ID_EXTRA"
const val ALLAT_START_ARG = "ALLAT_START_ARG"
const val ALLAT_END_ARG = "ALLAT_END_ARG"

const val NOTIFICATION_ID_ALLAT = 101
const val NOTIFICATION_ID_QUOTE = 102
const val NOTIFICATION_ID_RADIO = 103

fun showNotification(context: Context, notificationCode: AlarmNotificationCodes, extras: Bundle?) {
    //setup
    var remindBefore: Long = 0
    var remindBeforeUpdate: Long = 0
    var quote: String? = null
    var quotePosition: Int? = null
    var allatStartScheduled = false
    var allatEndScheduled = false

    extras?.let {
        remindBefore = extras.getLong(NOTIFICATION_BEFORE_MILLIS_EXTRAS)
        remindBeforeUpdate = extras.getLong(NOTIFICATION_BEFORE_MILLIS_UPDATE_EXTRAS, remindBefore)
        quote = extras.getString(NOTIFICATION_QUOTE_EXTRAS)
        quotePosition = extras.getInt(NOTIFICATION_QUOTE_POSITION_EXTRAS)
        allatStartScheduled = extras.getBoolean(ALLAT_START_ARG)
        allatEndScheduled = extras.getBoolean(ALLAT_END_ARG)
    }

    val title: String
    val text: String
    val action: String
    val notifChannelId: String
    val notifId: Int
    var allatSound = false
    var priority = PRIORITY_HIGH

    when (notificationCode) {
        AlarmNotificationCodes.ALLAT_START -> {
            title = context.getString(R.string.allat_reminder)
            text = context.getString(R.string.allat_has_started)
            action = NOTIFICATION_ACTION_ALLAT
            notifId = NOTIFICATION_ID_ALLAT
            notifChannelId = CHANNEL_ID_ALLAT
            allatSound = true
        }
        AlarmNotificationCodes.ALLAT_END -> {
            title = context.getString(R.string.allat_reminder)
            text = context.getString(R.string.allat_has_ended)
            action = NOTIFICATION_ACTION_ALLAT
            notifId = NOTIFICATION_ID_ALLAT
            notifChannelId = CHANNEL_ID_ALLAT
            allatSound = true
        }
        AlarmNotificationCodes.ALLAT_BEFORE -> {
            title = context.getString(R.string.allat_reminder)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remindBefore).toInt()
            text = context.resources.getQuantityString(R.plurals.mins_to_allat, minutes, minutes)
            action = NOTIFICATION_ACTION_ALLAT
            notifId = NOTIFICATION_ID_ALLAT
            notifChannelId = CHANNEL_ID_ALLAT
            allatSound = true
        }
        AlarmNotificationCodes.ALLAT_BEFORE_UPDATE -> {
            title = context.getString(R.string.allat_reminder)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remindBeforeUpdate).toInt()
            text = context.resources.getQuantityString(R.plurals.mins_to_allat, minutes, minutes)
            action = NOTIFICATION_ACTION_BEFORE_UPDATE
            notifId = NOTIFICATION_ID_ALLAT //or NOTIFICATION_ID_ALLAT_BEFORE_UPDATE
            notifChannelId = CHANNEL_ID_ALLAT_SILENCE
            priority = PRIORITY_LOW
        }
        AlarmNotificationCodes.QUOTE -> {
            if (quote?.isNotEmpty() == true) {
                title = context.getString(R.string.random_quote)
                text = quote!!
                action = NOTIFICATION_ACTION_QUOTE
                notifId = NOTIFICATION_ID_QUOTE
                notifChannelId = CHANNEL_ID_QUOTES
            } else {
                return
            }
        }
        else -> return
    }

    //notify
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val nBuilder = getBasicNotificationBuilder(context, notifChannelId, allatSound)
    nBuilder.setContentTitle(title)
        .setContentText(text)
        .setVibrate(longArrayOf(500, 500, 500, 500))
        .setContentIntent(getActivityPendingIntent(context, action, extras, javaClass = MainActivity::class.java))
        .priority = priority

    if (notificationCode == AlarmNotificationCodes.QUOTE && quotePosition != null) {
        nBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
    }


    if (notificationCode == AlarmNotificationCodes.ALLAT_BEFORE ||
        notificationCode == AlarmNotificationCodes.ALLAT_BEFORE_UPDATE) {
        nBuilder.setDeleteIntent(getRemoveAllatBeforeUpdatesIntent(context))
        Log.d("NotificationTimer", "timerSetDeleteIntent")
    }

    //backup if Job was killed and not yet come back
    //to hide it, but still > 9 mins from last and to next, to have all alarms working
//    if (notificationCode == AlarmNotificationCodes.ALLAT_BEFORE && TimeUnit.MILLISECONDS.toMinutes(remindBefore) >= 20) {
//        setAlarmRemainingTime(context,
//            remindBefore - TimeUnit.MINUTES.toMillis(9) - TimeUnit.SECONDS.toMillis(45),
//            AlarmNotificationCodes.CANCEL.action,
//            AlarmNotificationCodes.CANCEL.code,
//            Bundle().apply { putInt(NOTIFICATION_CANCEL_ID_EXTRA, notifId) })
//    }

    //cancel action
    if (notificationCode == AlarmNotificationCodes.ALLAT_START && !allatEndScheduled) {
        setAlarmRemainingTime(context, TimeUnit.MINUTES.toMillis(12) + 12000,
            AlarmNotificationCodes.CANCEL.action,
            AlarmNotificationCodes.CANCEL.code,
            Bundle().apply { putInt(NOTIFICATION_CANCEL_ID_EXTRA, notifId) })
    }

    if (notificationCode == AlarmNotificationCodes.ALLAT_END) {
        setAlarmRemainingTime(context, TimeUnit.MINUTES.toMillis(12),
            AlarmNotificationCodes.CANCEL.action,
            AlarmNotificationCodes.CANCEL.code,
            Bundle().apply { putInt(NOTIFICATION_CANCEL_ID_EXTRA, notifId) })
    }

    val notification = nBuilder.build()
    nManager.createNotificationChannel(context, notification, allatSound, priority)
    nManager.notify(notifId, notification)
}

fun getRadioNotification(context: Context, playbackState: AllatRadioService.PlaybackStatus, mediaSession: MediaSessionCompat): Notification {
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nManager.createRadioNotificationChannel()

    val controller = mediaSession.controller
    val mediaMetadata = controller.metadata
    val description = mediaMetadata.description

    val builder = NotificationCompat.Builder(context, NOTIFICATION_ACTION_RADIO)
        .setContentTitle(description.title)
        .setContentText(description.subtitle)
        .setSubText(description.description)
        .setLargeIcon(ContextCompat.getDrawable(context, R.drawable.radio_notif_bg)?.toBitmap())
        .setSmallIcon(R.drawable.allatra_small)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(getActivityPendingIntent(
            context,
            NOTIFICATION_ACTION_RADIO,
            javaClass = MainActivity::class.java
        ))
        .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))

    if (playbackState == AllatRadioService.PlaybackStatus.PLAYING ||
        playbackState == AllatRadioService.PlaybackStatus.BUFFERING) {
        builder.addAction(
                R.drawable.ic_pause,
                context.getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE)
        )
    } else {
        builder.addAction(
                R.drawable.ic_play,
                context.getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE)
        )
    }

    builder.addAction(
            R.drawable.ic_stop,
            context.getString(R.string.stop),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
    )

    builder.setStyle(
        androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1)
            .setShowCancelButton(true)
            .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
            .setMediaSession(mediaSession.sessionToken)
    )
    builder.priority = NotificationCompat.PRIORITY_DEFAULT //PRIORITY_HIGH
    builder.setChannelId(CHANNEL_ID_RADIO)
    builder.setOnlyAlertOnce(true)
    builder.setShowWhen(false)
    return builder.build()
}

fun showNotification(context: Context, id: Int, notification: Notification) {
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nManager.notify(id, notification)
}

fun hideAllatNotification(context: Context) {
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nManager.cancel(NOTIFICATION_ID_ALLAT)
}

fun hideNotification(context: Context, extras: Bundle?) {
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val cancelId = extras?.getInt(NOTIFICATION_CANCEL_ID_EXTRA, -1) ?: -1
    if (cancelId != -1) {
        nManager.cancel(cancelId)
    }
}

private fun getBasicNotificationBuilder(context: Context, channelId: String, allatSound: Boolean)
        : NotificationCompat.Builder {
    val nBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.allatra_small)
        .setLights(Color.BLUE, 500, 3000)
        .setAutoCancel(true)

    if (allatSound) {
        val notificationSound: Uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.ring)
        nBuilder.setSound(notificationSound)
    }

    return nBuilder
}

fun <T> getActivityPendingIntent(context: Context, action: String, extras: Bundle? = null,
                                 flag: Int = PendingIntent.FLAG_CANCEL_CURRENT,
                                 javaClass: Class<T>): PendingIntent? {
    val resultIntent = Intent(context, javaClass)
    resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    resultIntent.action = action
    extras?.let { resultIntent.putExtras(it) }

    return PendingIntent.getActivity(
        context,
        0,
        resultIntent,
        flag
    )
}

private fun getRemoveAllatBeforeUpdatesIntent(context: Context): PendingIntent {
    val cancelIntent = Intent(context, TimerExpiredReceiver::class.java)
    cancelIntent.action = AlarmNotificationCodes.CANCEL_ALLAT_UPDATE.action

    return PendingIntent.getBroadcast(
        context,
        AlarmNotificationCodes.CANCEL_ALLAT_UPDATE.code,
        cancelIntent,
        PendingIntent.FLAG_CANCEL_CURRENT
    )
}

@TargetApi(26)
private fun NotificationManager.createNotificationChannel(
    context: Context,
    notification: Notification,
    allatSound: Boolean,
    priority: Int
) {
    try {
        val channelID = notification.channelId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.getNotificationChannel(channelID) == null) {
            val channelName = when (channelID) {
                CHANNEL_ID_ALLAT -> CHANNEL_NAME_ALLAT
                CHANNEL_ID_ALLAT_SILENCE -> CHANNEL_NAME_ALLAT_SILENCED
                CHANNEL_ID_QUOTES -> CHANNEL_NAME_QUOTES
                CHANNEL_ID_RADIO -> CHANNEL_NAME_RADIO
                else -> "DefaultChannel"
            }

            val channelImportance = if (priority >= PRIORITY_HIGH) IMPORTANCE_HIGH else IMPORTANCE_LOW
            val nChannel = NotificationChannel(channelID, channelName, channelImportance)
            nChannel.enableLights(true)
            nChannel.lightColor = Color.BLUE

            nChannel.vibrationPattern = longArrayOf(500, 500, 500, 500)

            if (allatSound) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                nChannel.setSound(
                    Uri.parse("android.resource://" + context.packageName + "/" + R.raw.ring),
                    audioAttributes
                )
            }

            this.createNotificationChannel(nChannel)
        }
    } catch (e: NoSuchMethodError) {
        Log.e("NotificationHelper", e.message)
    }
}

@TargetApi(26)
private fun NotificationManager.createRadioNotificationChannel() {
    try {
        if (this.getNotificationChannel(CHANNEL_ID_RADIO) == null) {
            this.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_RADIO,
                    CHANNEL_NAME_RADIO,
                    NotificationManager.IMPORTANCE_DEFAULT //IMPORTANCE_HIGH to show expanded for few sec
                )
            )
        }
    } catch (e: NoSuchMethodError) {
        Log.e("NotificationHelper", e.message)
    }
}
