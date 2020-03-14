package com.allat.mboychenko.silverthread.presentation.viewstate

import androidx.paging.PagedList
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeNoteItem
import com.allat.mboychenko.silverthread.presentation.models.PracticesSort
import java.util.*

data class PracticeViewState(val data: PagedList<PracticeNoteItem>? = null,
                             val uniqPracticeNames: List<String>? = null,
                             val filterPractice: String? = null,
                             val filterDate: Pair<Calendar, Calendar?>? = null,
                             val sortState: PracticesSort = PracticesSort.TIME,
                             val searchState: String? = null,
                             val loading: Boolean = true) {
    fun filtersInInitState() =
        sortState == PracticesSort.TIME && filterPractice == null && searchState == null && filterDate == null
}