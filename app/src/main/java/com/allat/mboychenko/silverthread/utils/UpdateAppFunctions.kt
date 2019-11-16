package com.allat.mboychenko.silverthread.utils

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.allat.mboychenko.silverthread.BuildConfig
import com.allat.mboychenko.silverthread.data.storage.Storage
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.domain.interactor.AllatNotificationsInteractor
import com.allat.mboychenko.silverthread.domain.interactor.FileLoaderDetailsInteractor
import com.allat.mboychenko.silverthread.presentation.helpers.*

fun updateVersion(context: Context) {
    runTaskOnBackground(ExecutorThread.IO) {
        val storage: Storage = StorageImplementation(context)
        val lastUpdatedVersion = storage.getIntDefault(LAST_UPDATE_VERSION_PREF, 0)

        if (lastUpdatedVersion < BuildConfig.VERSION_CODE) {
            updateScript(context, storage)
        }
    }
}

private fun updateScript(context: Context, storage: Storage) {

    if (!storage.getBoolean(PATCH_25_APPLIED_PREF_KEY, false)) {
        applyPatchVer25(context, storage)
        storage.putBoolean(PATCH_25_APPLIED_PREF_KEY, true)
    }

    val allatStorage = AllatNotificationsInteractor(storage)
    reInitTimers(context,
        allatStorage.getAllatTimezone(),
        allatStorage.getAllatNotificationBeforeMins(),
        allatStorage.getAllatNotificationStart(),
        allatStorage.getAllatNotificationEnd(),
        true)

    storage.putInt(LAST_UPDATE_VERSION_PREF, BuildConfig.VERSION_CODE)
}

private fun applyPatchVer25(context: Context, storage: Storage) {
    val deprecatedPref = "BOOKS_DOWNLOADS_IDS_PREF_KEY"
    val oldDownloads: Map<String, Long> = storage.getMap(deprecatedPref)
    oldDownloads.isEmpty().let { empty ->
        if(!empty) {
            FileLoaderDetailsInteractor(storage).putAllLoadingIds(oldDownloads)
        }
    }
    storage.remove(deprecatedPref)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager.deleteNotificationChannel("quotes_notif")
    }
}


private const val LAST_UPDATE_VERSION_PREF = "LAST_UPDATE_VERSION_PREF"

private const val PATCH_25_APPLIED_PREF_KEY = "PATCH_25_APPLIED_PREF_KEY"
