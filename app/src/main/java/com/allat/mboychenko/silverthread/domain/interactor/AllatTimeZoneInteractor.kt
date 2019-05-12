package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.Storage

class AllatTimeZoneInteractor(val storage: Storage) : AllatTimeZoneStorage {

    override fun putAllatNotificationBefore(mins: Int) {
        storage.putInt(ALLAT_NOTIFICATION_BEFORE_PREF_KEY, mins)
    }

    override fun getAllatNotificationBeforeMins(): Int =
        storage.getIntDefault(ALLAT_NOTIFICATION_BEFORE_PREF_KEY, -1)

    override fun removeAllatNotification() {
        storage.remove(ALLAT_NOTIFICATION_BEFORE_PREF_KEY)
    }

    override fun getAllatTimezone(): AllatTimeZone {
        val allatTimezoneInt = storage.getIntDefault(ALLAT_TIMEZONE_PREF_KEY, -1)
        return if (allatTimezoneInt != -1)  AllatTimeZone.values()[allatTimezoneInt] else AllatTimeZone.NOT_INIT
    }

    override fun putAllatTimezone(allatTimeZone: AllatTimeZone) {
        storage.putInt(ALLAT_TIMEZONE_PREF_KEY, allatTimeZone.ordinal)
    }

    companion object {
        private const val ALLAT_NOTIFICATION_BEFORE_PREF_KEY = "ALLAT_NOTIFICATION_BEFORE_PREF_KEY"
        private const val ALLAT_TIMEZONE_PREF_KEY = "ALLAT_TIMEZONE_PREF_KEY"
    }
}