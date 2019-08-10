package com.allat.mboychenko.silverthread.domain.interactor

interface AppSettingsStorage {
    fun appFirstLaunch(): Boolean
}