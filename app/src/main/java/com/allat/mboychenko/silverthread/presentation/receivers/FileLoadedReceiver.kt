package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService

class FileLoadedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        FileLoaderService.commandRefreshLoadings(context.applicationContext)
    }
}