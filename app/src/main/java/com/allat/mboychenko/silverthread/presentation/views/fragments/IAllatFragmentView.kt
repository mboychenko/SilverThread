package com.allat.mboychenko.silverthread.presentation.views.fragments

interface IAllatFragmentView {
    fun updateTimer(h: Long, m: Long, s: Long)
    fun updateTimerStatus(allatStatusTitle: String)
    fun changeTimezoneSetupVisibility(visible: Boolean)
}