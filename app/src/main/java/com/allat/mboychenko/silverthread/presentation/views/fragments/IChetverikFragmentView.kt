package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.presentation.models.ChetverikStage

interface IChetverikFragmentView {
    fun stageChanged(stage: ChetverikStage)
    fun getOffset(): Int
    fun setMinsViewOffset(min: Int)
    fun setSecondsViewOffset(sec: Int)
    fun stageTimeRemaining(mins:Int, sec: Int)
}
