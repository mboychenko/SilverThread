package com.allat.mboychenko.silverthread.presentation.models

import com.allat.mboychenko.silverthread.R

enum class PracticesSort(val resId: Int) {
    TIME(R.string.time), //default start time desc
    NAME(R.string.name), // title a-z + start time
    DURATION(R.string.duration_title); // desc
}