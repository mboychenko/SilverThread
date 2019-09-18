package com.allat.mboychenko.silverthread.utils

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.allat.mboychenko.silverthread.BuildConfig
import com.allat.mboychenko.silverthread.data.storage.Storage
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.domain.interactor.QuotesInteractor
import com.allat.mboychenko.silverthread.presentation.helpers.*

fun updateVersion(context: Context) {
    runTaskOnBackground(ExecutorThread.IO) {
        val storage: Storage = StorageImplementation(context)
        val lastUpdatedVersion = storage.getIntDefault(LAST_UPDATE_VERSION_PREF, 0)

        if (lastUpdatedVersion != BuildConfig.VERSION_CODE) {
            updateScript(context, storage)
        }
    }
}

private fun updateScript(context: Context, storage: Storage) {
    if (!storage.getBoolean(PATCH_24_APPLIED_PREF_KEY, false)) {
        applyPatchVer24(context, storage)
        storage.putBoolean(PATCH_24_APPLIED_PREF_KEY, true)
    }

    applyPatchVer25()

    storage.putInt(LAST_UPDATE_VERSION_PREF, BuildConfig.VERSION_CODE)
}

private fun applyPatchVer24(context: Context, storage: Storage) {
    removeAlarm(context, AlarmNotificationCodes.QUOTE.action, AlarmNotificationCodes.QUOTE.code)
    QuotesInteractor(storage).clearShowedTimesInDay()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            nManager.deleteNotificationChannel(CHANNEL_ID_QUOTES)
        } catch (e: NoSuchMethodError) {
            Log.e("UpdateAppFunction", e.message)
        }
    }

    setupRandomQuoteNextAlarm(context, forceNextDay = true)
}

private fun applyPatchVer25() {
    //future
}

private const val LAST_UPDATE_VERSION_PREF = "LAST_UPDATE_VERSION_PREF"

private const val PATCH_24_APPLIED_PREF_KEY = "PATCH_24_APPLIED_PREF_KEY"
