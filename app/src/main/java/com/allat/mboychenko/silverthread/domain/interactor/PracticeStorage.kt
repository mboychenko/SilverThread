package com.allat.mboychenko.silverthread.domain.interactor

interface PracticeStorage {
    fun setStartOffsetSeconds(seconds: Int)
    fun getStartOffsetSeconds(): Int
}