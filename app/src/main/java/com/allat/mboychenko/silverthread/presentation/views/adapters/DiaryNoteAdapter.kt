package com.allat.mboychenko.silverthread.presentation.views.adapters

import android.view.ViewGroup
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.listitems.DiaryNoteItem
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener
import com.allat.mboychenko.silverthread.presentation.views.viewholders.DiaryNoteViewHolder

class DiaryNoteAdapter(
    private val actionListener: INoteActionListener
): BaseDiaryAdapter<DiaryNoteItem, DiaryNoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DiaryNoteViewHolder(
            parent,
            actionListener,
            R.layout.diary_note_item
        )

}