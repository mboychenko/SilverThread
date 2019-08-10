package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.Storage

class AppSettingsStorageInteractor(val storage: Storage) : AppSettingsStorage {

    override fun appFirstLaunch(): Boolean {
        val firstLaunch = storage.getBoolean(APP_FIRST_LAUNCH, true)
        if (firstLaunch) {
            storage.putBoolean(APP_FIRST_LAUNCH, false)
        }
        return firstLaunch
    }

    companion object {
        const val APP_FIRST_LAUNCH = "APP_FIRST_LAUNCH"
    }
}