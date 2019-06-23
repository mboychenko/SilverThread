package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.setupRandomQuoteNextAlarm
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.dialogs.IQuotesNotificationSettingView
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.presenters.BasePresenter

class QuotesNotificationSettingsPresenter(private val context: Context,
                                          private val quotesStorage: QuotesDetailsStorage) : BasePresenter<IQuotesNotificationSettingView>() {

    fun getRandomQuoteTimesInDay() =
        quotesStorage.getRandomQuotesTimesInDay()

    fun setRandomQuoteTimesInDay(times: Int) {
        runTaskOnComputation {
            quotesStorage.setRandomQuotesTimesInDay(times)
            if (times == 0) {
                removeAlarm(context, AlarmNotificationCodes.QUOTE.action, AlarmNotificationCodes.QUOTE.code)
            } else {
                setupRandomQuoteNextAlarm(context)
            }
        }
    }
}