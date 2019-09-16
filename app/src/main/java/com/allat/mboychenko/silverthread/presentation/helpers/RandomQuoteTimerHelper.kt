package com.allat.mboychenko.silverthread.presentation.helpers

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation
import com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneInteractor
import com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneStorage
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.domain.interactor.QuotesInteractor
import java.util.*
import java.util.concurrent.TimeUnit

fun setupRandomQuoteNextAlarm(context: Context, fromNotification: Boolean = false, forceNextDay: Boolean = false) {
    val storageImplementation = StorageImplementation(context)
    val allatStorage: AllatTimeZoneStorage = AllatTimeZoneInteractor(storageImplementation)
    val quotesStorage: QuotesDetailsStorage = QuotesInteractor(storageImplementation)

    val randomQuotesInDay = quotesStorage.getRandomQuotesTimesInDay()
    if (randomQuotesInDay != 0) {

        val allatTimezone = getAllatTimezone(allatStorage.getAllatTimezone())

        val (showedTimes, dayInMillis) = quotesStorage.getQuotesWasShowedTimesInDay()

        val lastNotifDay = Calendar.getInstance()
        lastNotifDay.timeInMillis = dayInMillis

        var nextNotificationTime: Long = 0

        val now = Calendar.getInstance()
        if (lastNotifDay.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH)) {
             nextNotificationTime = getMillisToNextQuote(allatTimezone, dayInMillis)
        } else if (lastNotifDay.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
            nextNotificationTime = if (showedTimes < randomQuotesInDay && !forceNextDay) {
                getMillisToNextQuote(allatTimezone, dayInMillis)
            } else {
                getMillisToNextQuote(allatTimezone, dayInMillis, true)
            }
        }

        val (position, quote) = getRandomQuote(context)
        setAlarmExactTime(context, nextNotificationTime,
            AlarmNotificationCodes.QUOTE.action, AlarmNotificationCodes.QUOTE.code,
            Bundle().apply {
                putInt(NOTIFICATION_QUOTE_POSITION_EXTRAS, position)
                putString(NOTIFICATION_QUOTE_EXTRAS, quote)
            }
        )


        if (fromNotification) {
            quotesStorage.setQuotesWasShowedTimesInDay(showedTimes + 1 to Calendar.getInstance().timeInMillis)
        }

    }
}

private fun getMillisToNextQuote(allatTimezone: AllatTimeZone, lastNotificationTime: Long, nextDay: Boolean = false): Long {
    val now = Calendar.getInstance()
    var changeToNextDay = nextDay
    val allatTimeOffsets: AllatTimeOffsets
    Log.d("LogQuotes", "now ${now.timeInMillis}")
    Log.d("LogQuotes", "nextDay $nextDay")
    Log.d("LogQuotes", "lastNotificationTime $lastNotificationTime")

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val limits = Calendar.getInstance()
    limits.set(Calendar.MINUTE, 0)
    limits.set(Calendar.SECOND, 0)

    limits.set(Calendar.HOUR_OF_DAY, 8)
    var startLimit = limits.timeInMillis

    limits.set(Calendar.HOUR_OF_DAY, 22)
    var endLimit = limits.timeInMillis

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    if(now.timeInMillis > startLimit && !changeToNextDay) {
        if (now.timeInMillis + TimeUnit.MINUTES.toMillis(5) < endLimit) {
            startLimit = now.timeInMillis + TimeUnit.MINUTES.toMillis(5)
        } else {
            changeToNextDay = true
        }
    }

    val startLimitFromLastNotif = lastNotificationTime + TimeUnit.MINUTES.toMillis(127)
    if (startLimitFromLastNotif > startLimit && !changeToNextDay ||
        startLimitFromLastNotif > endLimit && !changeToNextDay) {
        if (startLimitFromLastNotif < endLimit) {
            startLimit = startLimitFromLastNotif
        } else {
            changeToNextDay = true
        }
    }

    if (changeToNextDay || now.timeInMillis >= endLimit) {
        now.set(Calendar.HOUR_OF_DAY, 23)
        now.timeInMillis += TimeUnit.MINUTES.toMillis(127)      //next day

        allatTimeOffsets = setupAllatTimeOffsets(now, allatTimezone)

        //next day start limit
        now.set(Calendar.HOUR_OF_DAY, 8)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        startLimit = now.timeInMillis

        //next day end limit
        now.set(Calendar.HOUR_OF_DAY, 22)
        endLimit = now.timeInMillis
    } else {
        allatTimeOffsets = setupAllatTimeOffsets(now, allatTimezone)
    }

    Log.d("LogQuotes", "changeToNextDay $changeToNextDay")
    Log.d("LogQuotes", "startLim $startLimit")
    Log.d("LogQuotes", "endLim $endLimit")
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    var generatedLong: Long
    var counter = 0
    do {
        counter++
        if (counter > 12) {
            Log.d("LogQuotes", "counter more than 12")
            return getMillisToNextQuote(allatTimezone, lastNotificationTime, true)
        }
        generatedLong = startLimit + (Math.random() * (endLimit - startLimit)).toLong()
        Log.d("LogQuotes", "generatedTime $generatedLong")
    } while (!timeInAvailableRange(generatedLong,
            allatTimeOffsets.morningTimeBeforeOffset,
            allatTimeOffsets.morningTimeAfterOffset,
            allatTimeOffsets.eveningTimeBeforeOffset,
            allatTimeOffsets.eveningTimeAfterOffset))

    Log.d("LogQuotes", "counter $counter")
    Log.d("LogQuotes", "time $generatedLong")
    return generatedLong
}

private fun setupAllatTimeOffsets(now: Calendar, allatTimezone: AllatTimeZone): AllatTimeOffsets {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now.timeInMillis
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    calendar.set(Calendar.HOUR_OF_DAY, allatTimezone.morningTime)
    val morningTimeBeforeOffset = calendar.timeInMillis - TimeUnit.MINUTES.toMillis(30)
    val morningTimeAfterOffset = calendar.timeInMillis + TimeUnit.MINUTES.toMillis(50)

    calendar.set(Calendar.HOUR_OF_DAY, allatTimezone.eveningTime)
    val eveningTimeBeforeOffset = calendar.timeInMillis - TimeUnit.MINUTES.toMillis(30)
    val eveningTimeAfterOffset = calendar.timeInMillis + TimeUnit.MINUTES.toMillis(50)

    return AllatTimeOffsets(
        morningTimeBeforeOffset,
        morningTimeAfterOffset,
        eveningTimeBeforeOffset,
        eveningTimeAfterOffset)
}

private fun timeInAvailableRange(
    generatedLong: Long,
    morningTimeBeforeOffset: Long,
    morningTimeAfterOffset: Long,
    eveningTimeBeforeOffset: Long,
    eveningTimeAfterOffset: Long
): Boolean =
    generatedLong !in morningTimeBeforeOffset..morningTimeAfterOffset &&
            generatedLong !in eveningTimeBeforeOffset..eveningTimeAfterOffset


private fun getRandomQuote(context: Context): Pair<Int, String> {
    val quotes = context.resources.getStringArray(R.array.quotes)
    val position = Random().nextInt(quotes.size)
   return Pair(position, quotes[position])
}

private fun getAllatTimezone(allatTimezone: AllatTimeZone) =
    if (allatTimezone == AllatTimeZone.NOT_INIT) {
        AllatTimeZone.LOCAL
    } else {
        allatTimezone
    }

private class AllatTimeOffsets(
    val morningTimeBeforeOffset: Long,
    val morningTimeAfterOffset: Long,
    val eveningTimeBeforeOffset: Long,
    val eveningTimeAfterOffset: Long
)