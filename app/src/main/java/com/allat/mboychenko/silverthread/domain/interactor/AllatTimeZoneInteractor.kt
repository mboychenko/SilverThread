package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.Storage

class AllatTimeZoneInteractor(val storage: Storage) : AllatTimeZoneStorage {

    override fun getAllatTimezone(): AllatTimeZone =
        AllatTimeZone.values()[storage.getInt(ALLAT_TIMEZONE_PREF_KEY)]

    override fun putAllatTimezone(allatTimeZone: AllatTimeZone) {
        storage.putInt(ALLAT_TIMEZONE_PREF_KEY, allatTimeZone.ordinal)
    }

    companion object {
        private const val ALLAT_TIMEZONE_PREF_KEY = "ALLAT_TIMEZONE_PREF_KEY"
    }
}