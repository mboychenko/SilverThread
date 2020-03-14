package com.allat.mboychenko.silverthread.presentation.viewstate

import androidx.paging.PagedList
import com.allat.mboychenko.silverthread.presentation.views.listitems.DiaryNoteItem
import java.util.*

data class DiaryViewState(val data: PagedList<DiaryNoteItem>? = null,
                          val filterDate: Pair<Calendar, Calendar?>? = null,
                          val searchState: String? = null,
                          val loading: Boolean = true) {

    fun hasFilters() = filterDate != null || !searchState.isNullOrEmpty()

}