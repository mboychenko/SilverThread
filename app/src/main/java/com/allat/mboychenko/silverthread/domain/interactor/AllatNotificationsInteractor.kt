package com.allat.mboychenko.silverthread.domain.interactor

import android.content.Context
import com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.data.storage.preferences.Storage
import com.allat.mboychenko.silverthread.data.storage.preferences.StorageImplementation

class AllatNotificationsInteractor(private val storage: Storage) : AllatNotificationsSettingsStorage {

    constructor(context: Context) : this(
        StorageImplementation(
            context
        )
    )

    override fun allatNotificationStart(enabled: Boolean) {
        storage.putBoolean(ALLAT_START_PREF_KEY, enabled)
    }

    override fun getAllatNotificationStart(): Boolean =
        storage.getBoolean(ALLAT_START_PREF_KEY, false)

    override fun allatNotificationEnd(enabled: Boolean) {
        storage.putBoolean(ALLAT_END_PREF_KEY, enabled)
    }

    override fun getAllatNotificationEnd(): Boolean =
        storage.getBoolean(ALLAT_END_PREF_KEY, false)

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

    override fun allatSoundLoud(): Boolean =
        storage.getBoolean(ALLAT_SOUND_LOUD_KEY, false)

    override fun setAllatSoundLoud(loud: Boolean)  {
        storage.putBoolean(ALLAT_SOUND_LOUD_KEY, loud)
    }

    companion object {
        private const val ALLAT_NOTIFICATION_BEFORE_PREF_KEY = "ALLAT_NOTIFICATION_BEFORE_PREF_KEY"
        private const val ALLAT_TIMEZONE_PREF_KEY = "ALLAT_TIMEZONE_PREF_KEY"
        private const val ALLAT_START_PREF_KEY = "ALLAT_START_PREF_KEY"
        private const val ALLAT_END_PREF_KEY = "ALLAT_END_PREF_KEY"
        private const val ALLAT_SOUND_LOUD_KEY = "ALLAT_SOUND_LOUD_KEY"
    }
}