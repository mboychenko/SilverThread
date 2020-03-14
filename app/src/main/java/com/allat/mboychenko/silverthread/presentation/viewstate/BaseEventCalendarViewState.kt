package com.allat.mboychenko.silverthread.presentation.viewstate

import java.util.*

open class BaseEventCalendarViewState(open val data: List<Pair<Calendar, Int>>?,
                                      open val loading: Boolean)