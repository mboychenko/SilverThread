package com.allat.mboychenko.silverthread.utils

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.WorkManager
import com.allat.mboychenko.silverthread.BuildConfig
import com.allat.mboychenko.silverthread.data.storage.preferences.Storage
import com.allat.mboychenko.silverthread.data.storage.preferences.StorageImplementation
import com.allat.mboychenko.silverthread.domain.interactor.FileLoaderDetailsInteractor
import com.allat.mboychenko.silverthread.domain.interactor.ParablesInteractor
import com.allat.mboychenko.silverthread.presentation.helpers.*

fun updateVersion(context: Context, workManager: WorkManager) {
    runTaskOnBackground(ExecutorThread.IO) {
        val storage: Storage = StorageImplementation(context)
        val lastUpdatedVersion = storage.getIntDefault(LAST_UPDATE_VERSION_PREF, 0)

        if (lastUpdatedVersion < BuildConfig.VERSION_CODE) {
            updateScript(context, storage, workManager, lastUpdatedVersion)
        }

        storage.putInt(LAST_UPDATE_VERSION_PREF, BuildConfig.VERSION_CODE)
    }
}

private fun updateScript(
    context: Context,
    storage: Storage,
    workManager: WorkManager,
    lastUpdatedVersion: Int
) {

    if (!storage.getBoolean(PATCH_25_APPLIED_PREF_KEY, false)) {
        applyPatchVer25(context, storage)
        storage.putBoolean(PATCH_25_APPLIED_PREF_KEY, true)
    }

    if (!storage.getBoolean(PATCH_45_APPLIED_PREF_KEY, false)) {
        applyPatchVer45(workManager)
        storage.putBoolean(PATCH_45_APPLIED_PREF_KEY, true)
    }

    if (!storage.getBoolean(PATCH_52_APPLIED_PREF_KEY, false)) {
        applyPatchVer52(workManager)
        storage.putBoolean(PATCH_52_APPLIED_PREF_KEY, true)
    }

    if (!storage.getBoolean(PATCH_53_APPLIED_PREF_KEY, false) &&
        (lastUpdatedVersion != 0 && lastUpdatedVersion < 54)) {
        applyPatchVer53(storage)
        storage.putBoolean(PATCH_53_APPLIED_PREF_KEY, true)
    }

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

private fun applyPatchVer45(wm: WorkManager) {
     wm.cancelUniqueWork("DAILY_TIMERS_CHECKER_WORK_TAG") //deprecated tag
}

private fun applyPatchVer52(wm: WorkManager) {
    applyPatchVer45(wm)
    wm.cancelUniqueWork("HACK_INIT_DAILY_WORK_TAG") //deprecated tag
}

private fun applyPatchVer53(
    storage: Storage
) {
    val parStorage = ParablesInteractor(storage)
    val parables = parStorage.getFavoriteParablesPositions().toMutableSet()
    parables.remove(7)
    val result = parables.map {
        if (it >= 8) it - 1 else it
    }.toSet()
    parStorage.rewriteFavoriteParablesPositions(result)
}

private const val LAST_UPDATE_VERSION_PREF = "LAST_UPDATE_VERSION_PREF"

private const val PATCH_25_APPLIED_PREF_KEY = "PATCH_25_APPLIED_PREF_KEY"
private const val PATCH_45_APPLIED_PREF_KEY = "PATCH_45_APPLIED_PREF_KEY"
private const val PATCH_52_APPLIED_PREF_KEY = "PATCH_52_APPLIED_PREF_KEY"
private const val PATCH_53_APPLIED_PREF_KEY = "PATCH_53_APPLIED_PREF_KEY"
