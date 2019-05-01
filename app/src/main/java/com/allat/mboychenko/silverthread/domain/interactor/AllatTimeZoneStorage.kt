package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone

interface AllatTimeZoneStorage {

    fun getAllatTimezone(): AllatTimeZone
    fun putAllatTimezone(allatTimeZone: AllatTimeZone)

}