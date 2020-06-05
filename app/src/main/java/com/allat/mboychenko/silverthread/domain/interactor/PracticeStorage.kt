package com.allat.mboychenko.silverthread.domain.interactor

interface PracticeStorage {
    fun setStartOffsetSeconds(seconds: Int)
    fun getStartOffsetSeconds(): Int
    fun setAllatLengthStateShort(short: Boolean)
    fun getAllatLengthStateShort(): Boolean


    fun setVolumeStateHigh(short: Boolean)
    fun getVolumeStateHigh(): Boolean
//    fun setVolumeHigh(short: Boolean)
//    for default saved choice of user API methods
//    fun getVolumeHigh(): Boolean

}