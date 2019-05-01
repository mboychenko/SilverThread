package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            //todo check prefs and reinit notification timer if need
        }
    }

}