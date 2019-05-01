package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models

enum class AllatTimeZone(val timeZone: String, val morningTime: Int, val eveningTime: Int) {
    KIEV("Europe/Kiev", 9, 21),
    GMT("GMT", 7, 19),
    LOCAL("", 9, 21)
}