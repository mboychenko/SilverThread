package com.allat.mboychenko.silverthread.domain.interactor

interface PracticeStorage {
    fun setStartOffsetSeconds(seconds: Int)
    fun getStartOffsetSeconds(): Int
    fun setAllatLengthStateShort(short: Boolean)
    fun getAllatLengthStateShort(): Boolean


    fun setVolumeStateHigh(high: Boolean)
    fun getVolumeStateHigh(): Boolean


}