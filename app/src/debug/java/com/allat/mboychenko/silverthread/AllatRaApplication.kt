package com.allat.mboychenko.silverthread

import com.allat.mboychenko.silverthread.presentation.helpers.getPublicDownloadsStorageDir

class AllatRaApplication: BaseApplication() {

    override fun onCreate() {
        saveLogcatToFile()
        super.onCreate()
    }

    private fun saveLogcatToFile() {
        val fileName = "/logcat_" + System.currentTimeMillis() + ".txt"
        val outputFile = getPublicDownloadsStorageDir("allat_logcat")
        Runtime
            .getRuntime()
            .exec("logcat NotificationHelper:D AlarmManager:D LogQuotes:D *:S -f " + outputFile?.absolutePath + fileName)
    }

}