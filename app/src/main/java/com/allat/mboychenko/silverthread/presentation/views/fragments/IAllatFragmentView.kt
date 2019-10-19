package com.allat.mboychenko.silverthread.presentation.views.fragments

interface IAllatFragmentView {
    fun updateAllatTimer(h: Long, m: Long, s: Long)
    fun updateTimerStatus(allatStatusTitle: String)
    fun changeTimezoneSetupVisibility(visible: Boolean)
    fun openDrawer()
    fun ringOnStartEnabled(enabled: Boolean)
    fun ringOnEndEnabled(enabled: Boolean)
    fun setAllatSoundLoud(loud: Boolean)
    fun allatNotifIn(minutes: Int)
    fun updateVerdictTimer(days: Int, hoursLeft: Int, minsLeft: Int)
    fun updateVerdictTitle(title: String)
    fun updateVerdictSubtitle(subtitle: String)
}