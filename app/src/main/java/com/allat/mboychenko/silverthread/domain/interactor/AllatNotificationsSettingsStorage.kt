package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.models.AllatTimeZone

interface AllatNotificationsSettingsStorage {

    fun getAllatTimezone(): AllatTimeZone
    fun putAllatTimezone(allatTimeZone: AllatTimeZone)
    fun putAllatNotificationBefore(mins: Int)
    fun getAllatNotificationBeforeMins(): Int
    fun removeAllatNotification()
    fun allatNotificationStart(enabled: Boolean)
    fun getAllatNotificationStart(): Boolean
    fun allatNotificationEnd(enabled: Boolean)
    fun getAllatNotificationEnd(): Boolean
    fun allatSoundLoud(): Boolean
    fun setAllatSoundLoud(loud: Boolean)
}