package com.allat.mboychenko.silverthread.presentation.views.listitems

import com.allat.mboychenko.silverthread.presentation.models.ItemIdentifiable
import java.util.*

data class PracticeNoteItem(
    val noteId: String,
    val title: String,
    val note: String?,
    val startDate: Calendar,
    val endDate: Calendar?,
    val duration: Long):
    ItemIdentifiable {

    override fun id() = noteId

}