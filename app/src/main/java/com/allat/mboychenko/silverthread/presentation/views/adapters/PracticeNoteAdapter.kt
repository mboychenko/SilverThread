package com.allat.mboychenko.silverthread.presentation.views.adapters

import android.view.ViewGroup
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeNoteItem
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener
import com.allat.mboychenko.silverthread.presentation.views.viewholders.PracticeNoteItemViewHolder

class PracticeNoteAdapter(
    private val actionListener: INoteActionListener
) :
    BaseDiaryAdapter<PracticeNoteItem, PracticeNoteItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PracticeNoteItemViewHolder(
            parent,
            actionListener,
            R.layout.practice_note_item
        )

}