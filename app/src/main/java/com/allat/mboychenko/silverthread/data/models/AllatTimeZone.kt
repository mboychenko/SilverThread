package com.allat.mboychenko.silverthread.data.models

import java.util.TimeZone


enum class AllatTimeZone(val timeZone: String = "", val morningTime: Int = 0, val eveningTime: Int = 0) {
    KIEV("Europe/Kiev", 9, 21),
    GMT("GMT", 7, 19),
    LOCAL("", 9, 21),
    NOT_INIT;

    fun getCalendarTimezone(): TimeZone =
        when (this) {
            KIEV, GMT -> {
                TimeZone.getTimeZone(timeZone)
            }
            else -> {                                                       //local or init
                TimeZone.getDefault()
            }
        }
}