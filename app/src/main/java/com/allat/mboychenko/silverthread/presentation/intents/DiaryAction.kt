package com.allat.mboychenko.silverthread.presentation.intents

import com.allat.mboychenko.silverthread.presentation.models.PracticesSort
import java.util.*

sealed class DiaryAction {
    object Load : DiaryAction()
    object ClearFilterSortSelections : DiaryAction()
    object ClearDateFilter : DiaryAction()
    data class FilterPractice(val title: String? = null) : DiaryAction()
    data class FilterDate(val filterDate: Pair<Calendar, Calendar?>) : DiaryAction()
    data class FilterDatePractice(val title: String? = null, val filterDate: Pair<Calendar, Calendar?>) : DiaryAction()
    data class Sort(val sort: PracticesSort) : DiaryAction()
    data class Search(val search: String) : DiaryAction()
}