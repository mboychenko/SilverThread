package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import java.util.*
import java.util.concurrent.TimeUnit

object AllatHelper {

    fun getTimeToAllat(allatTimeZone: AllatTimeZone): Pair<Long, TimeStatus> {
        val timeTo: Long
        val timeStatus: TimeStatus

        val timezone: TimeZone
        val morningTime: Int
        val eveningTime: Int

        when(allatTimeZone) {
            AllatTimeZone.LOCAL -> {
                timezone = TimeZone.getDefault()
                morningTime = allatTimeZone.morningTime
                eveningTime = allatTimeZone.eveningTime
            }
            AllatTimeZone.KIEV -> {
                timezone = TimeZone.getTimeZone(allatTimeZone.timeZone)
                morningTime = allatTimeZone.morningTime
                eveningTime = allatTimeZone.eveningTime
            }
            AllatTimeZone.GMT -> {
                timezone = TimeZone.getTimeZone(allatTimeZone.timeZone)
                morningTime = allatTimeZone.morningTime
                eveningTime = allatTimeZone.eveningTime
            }
        }

        val current = Calendar.getInstance(timezone)

        val nextMeditationTime = Calendar.getInstance(timezone)
        nextMeditationTime.set(Calendar.MINUTE, 0)
        nextMeditationTime.set(Calendar.SECOND, 0)

        val curHour = current.get(Calendar.HOUR_OF_DAY)

        if (curHour == eveningTime && current.get(Calendar.MINUTE) < 12) {
            val currentMeditationEnd = Calendar.getInstance(timezone)
            currentMeditationEnd.set(Calendar.MINUTE, 12)
            timeTo = currentMeditationEnd.timeInMillis - current.timeInMillis
            timeStatus = AllatHelper.TimeStatus.IN_MEDITATION
            return Pair(timeTo, timeStatus)
        }

        when {
            curHour < morningTime -> nextMeditationTime.set(Calendar.HOUR_OF_DAY, morningTime)
            curHour < eveningTime -> nextMeditationTime.set(Calendar.HOUR_OF_DAY, eveningTime)
            else -> {
                val nextDay = current.timeInMillis + TimeUnit.HOURS.toMillis(5)
                nextMeditationTime.timeInMillis = nextDay
                nextMeditationTime.set(Calendar.HOUR_OF_DAY, morningTime)
                nextMeditationTime.set(Calendar.MINUTE, 0)
                nextMeditationTime.set(Calendar.SECOND, 0)
            }
        }
        timeStatus = AllatHelper.TimeStatus.AWAITING
        timeTo = nextMeditationTime.timeInMillis - current.timeInMillis

        return Pair(timeTo, timeStatus)
    }

    enum class TimeStatus {
        IN_MEDITATION,
        AWAITING
    }
}