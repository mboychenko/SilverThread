package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.preferences.Storage
import java.util.*

class AppSettingsStorageInteractor(private val storage: Storage) : AppSettingsStorage {

    override fun appFirstLaunch(): Boolean {
        val firstLaunch = storage.getBoolean(APP_FIRST_LAUNCH, true)
        if (firstLaunch) {
            storage.putBoolean(APP_FIRST_LAUNCH, false)
        }
        return firstLaunch
    }

    override fun setBackupInterval(value: Int) {
        storage.putInt(APP_BACKUP_INTERVAL, value)
    }

    override fun getBackupInterval() = storage.getInt(APP_BACKUP_INTERVAL)

    override fun getLastBackupTime(): Long = storage.getLong(APP_BACKUP_LAST_TIME)

    override fun updateLastBackupTime() {
        storage.putLong(APP_BACKUP_LAST_TIME, Calendar.getInstance().timeInMillis)
    }

    companion object {
        const val APP_FIRST_LAUNCH = "APP_FIRST_LAUNCH"
        const val APP_BACKUP_INTERVAL = "APP_BACKUP_INTERVAL"
        const val APP_BACKUP_LAST_TIME = "APP_BACKUP_LAST_TIME"
    }
}