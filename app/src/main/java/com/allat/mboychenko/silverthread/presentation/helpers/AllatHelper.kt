package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import java.util.*
import java.util.concurrent.TimeUnit

object AllatHelper {

    fun getMillisToAllatStart(allatZone: AllatTimeZone, allatAfterNext: Boolean = false): Long {

        val allatTimezone = getTimezone(allatZone)
        val morningTime = allatZone.morningTime
        val eveningTime = allatZone.eveningTime

        val current = Calendar.getInstance(allatTimezone)
        var curHour = current.get(Calendar.HOUR_OF_DAY)

        if (allatAfterNext) {        //max reminder 60 min, so to reinit timer for next reminder we need offset
            curHour += 2
        }

        val nextMeditationTime= configureNextMeditationTime(current, curHour, morningTime, eveningTime, allatTimezone)

        return nextMeditationTime.timeInMillis - current.timeInMillis
    }

    fun getMillisToAllatEnd(allatZone: AllatTimeZone): Long {

        val allatTimezone = getTimezone(allatZone)
        val morningTime = allatZone.morningTime
        val eveningTime = allatZone.eveningTime

        val current = Calendar.getInstance(allatTimezone)
        val curHour = current.get(Calendar.HOUR_OF_DAY)

        if ((curHour == eveningTime || curHour == morningTime) && current.get(Calendar.MINUTE) < 12) {
            val meditationEnd = Calendar.getInstance(allatTimezone)
            meditationEnd.set(Calendar.MINUTE, 12)
            meditationEnd.set(Calendar.SECOND, 0)
            return meditationEnd.timeInMillis - current.timeInMillis
        }

        val nextMeditationTime = configureNextMeditationTime(current, curHour, morningTime, eveningTime, allatTimezone)
        nextMeditationTime.set(Calendar.MINUTE, 12)

        return nextMeditationTime.timeInMillis - current.timeInMillis
    }

    fun getAllatTimeStatus(allatZone: AllatTimeZone): Pair<Long, TimeStatus> {

        val allatTimezone: TimeZone = getTimezone(allatZone)
        val morningTime = allatZone.morningTime
        val eveningTime = allatZone.eveningTime

        val current = Calendar.getInstance(allatTimezone)
        val curHour = current.get(Calendar.HOUR_OF_DAY)

        if ((curHour == eveningTime || curHour == morningTime) && current.get(Calendar.MINUTE) < 12) {
            val meditationEnd = Calendar.getInstance(allatTimezone)
            meditationEnd.set(Calendar.MINUTE, 12)
            meditationEnd.set(Calendar.SECOND, 0)
            return Pair(meditationEnd.timeInMillis - current.timeInMillis, TimeStatus.IN_MEDITATION)
        }

        val nextMeditationTime = configureNextMeditationTime(current, curHour, morningTime, eveningTime, allatTimezone)

        return Pair(nextMeditationTime.timeInMillis - current.timeInMillis, TimeStatus.AWAITING)
    }

    private fun configureNextMeditationTime(
        current: Calendar,
        curHour: Int,
        morningTime: Int,
        eveningTime: Int,
        allatTimezone: TimeZone
    ): Calendar {
        val nextMeditationTime = Calendar.getInstance(allatTimezone)
        nextMeditationTime.set(Calendar.MINUTE, 0)
        nextMeditationTime.set(Calendar.SECOND, 0)

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

        return nextMeditationTime
    }

    private fun getTimezone(allatTimeZone: AllatTimeZone) = when(allatTimeZone) {
        AllatTimeZone.KIEV -> {
            TimeZone.getTimeZone(allatTimeZone.timeZone)
        }
        AllatTimeZone.GMT -> {
            TimeZone.getTimeZone(allatTimeZone.timeZone)
        }
        else -> {                                                       //local or init
            TimeZone.getDefault()
        }
    }

    enum class TimeStatus {
        IN_MEDITATION,
        AWAITING,
        INIT
    }
}