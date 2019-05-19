package com.allat.mboychenko.silverthread.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneInteractor
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneStorage
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.QuotesInteractor
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.QuotesStorage
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.presentation.helpers.*

class AlarmsResetReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_TIME_CHANGED ||
            intent?.action == Intent.ACTION_TIMEZONE_CHANGED
                ) {
            context?.let {
                val storage = StorageImplementation(it)
                val allatStorage = AllatTimeZoneInteractor(storage) as AllatTimeZoneStorage
                val quotesStorage = QuotesInteractor(storage) as QuotesStorage

                val allatTimezone = allatStorage.getAllatTimezone()

                reInitTimers(context, allatTimezone,
                    allatStorage.getAllatNotificationBeforeMins(),
                    allatStorage.getAllatNotificationStart(),
                    allatStorage.getAllatNotificationEnd())

                //todo quotesStorage notifications reinit

            }

        }
    }

}