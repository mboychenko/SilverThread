package com.allat.mboychenko.silverthread.presentation.viewstate

import java.util.*

data class PracticeCalendarViewState(
    override val data: List<Pair<Calendar, Int>>? = null,
    val uniqPracticeNames: List<String>? = null,
    val filterPractice: String? = null,
    override val loading: Boolean = true
) : BaseEventCalendarViewState(data, loading)