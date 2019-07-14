package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.R

interface IRadioFragmentView {

    fun getCurrentPlayerButtonsState(): RadioFragment.PlayerButtonsState
    fun updateOnlineStatus(online: Boolean)
    fun updateOnAirStatus(colorId: Int, textId: Int = R.string.on_air)
    fun stopButtonState()
    fun pauseButtonState()
    fun playButtonState()
    fun playButtonsIdleState()
    fun showProgress()
    fun hideProgress()

}