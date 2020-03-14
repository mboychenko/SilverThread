package com.allat.mboychenko.silverthread.presentation.views.listitems

import com.allat.mboychenko.silverthread.presentation.models.ItemIdentifiable
import java.util.*

data class DiaryNoteItem(
    val id: String,
    val note: String,
    val start: Calendar
): ItemIdentifiable {
    override fun id() = id
}