package com.allat.mboychenko.silverthread.domain.interactor

interface AppSettingsStorage {
    fun appFirstLaunch(): Boolean
    fun getBackupInterval(): Int
    fun setBackupInterval(value: Int)
    fun updateLastBackupTime()
    fun getLastBackupTime(): Long
    fun getEveryDayWorkTimes(): Int
    fun addOneEveryDayWork()
    fun clearEveryDayWork()
}