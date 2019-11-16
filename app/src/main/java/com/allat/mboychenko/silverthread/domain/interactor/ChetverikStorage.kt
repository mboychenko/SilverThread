package com.allat.mboychenko.silverthread.domain.interactor

interface ChetverikStorage {
    fun setStartOffsetSeconds(seconds: Int)
    fun getStartOffsetSeconds(): Int
}