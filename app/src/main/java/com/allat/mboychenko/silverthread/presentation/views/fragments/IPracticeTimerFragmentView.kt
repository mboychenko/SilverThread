package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.presentation.models.PracticeStage

interface IPracticeTimerFragmentView {
    fun stageChanged(stage: PracticeStage, curAllat: Int = 1)
    fun getOffset(): Int
    fun getAllatsNum(): Int
    fun getAllatLengthShort(): Boolean
    fun setAllatLengthShort(short: Boolean)

    fun setMinsViewOffset(min: Int)
    fun setSecondsViewOffset(sec: Int)
    fun stageTimeRemaining(mins:Int, sec: Int)

    fun getVolumeHigh(): Boolean
    fun setVolumeHigh(high: Boolean)
}
