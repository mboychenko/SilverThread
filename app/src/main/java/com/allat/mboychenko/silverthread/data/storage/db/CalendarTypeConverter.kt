package com.allat.mboychenko.silverthread.data.storage.db

import androidx.room.TypeConverter
import java.util.*

class CalendarTypeConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return if (value == null) {
            null
        } else {
            Calendar.getInstance().apply { timeInMillis = value }
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: Calendar?): Long? {
        return date?.timeInMillis
    }
}