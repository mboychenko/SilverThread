package com.allat.mboychenko.silverthread.presentation.views.viewholders

import android.text.format.DateFormat
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.listitems.DiaryNoteItem
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener

class DiaryNoteViewHolder(
    parentView: ViewGroup,
    actionListener: INoteActionListener,
    layoutId: Int
) : DiaryBaseViewHolder<DiaryNoteItem>(parentView, actionListener, layoutId) {

    private var notes: String? = null

    private var noteTime: TextView
    private var noteText: TextView

    private val loading: String by lazy { itemView.context.getString(R.string.downloading) }

    init {
        with(itemView) {
            setOnClickListener {
                AlertDialog.Builder(parentView.context)
                    .setTitle(R.string.observations_title)
                    .setMessage(notes)
                    .setNegativeButton(parentView.context.getString(R.string.hide)) { dialog, _ -> dialog.dismiss() }
                    .show()
            }

            findViewById<ImageView>(R.id.button_more).apply {
                initPopup(this)
                setOnClickListener { menuPopupHelper.show() }
            }

            noteTime = findViewById(R.id.note_time)
            noteText = findViewById(R.id.note)
        }
    }


    /**
     * Items might be null if they are not paged in yet. PagedListAdapter will re-bind the
     * ViewHolder when Item is loaded.
     */
    override fun bindTo(note: DiaryNoteItem?) {
        note?.let {
            noteId = it.id
            notes = it.note
            noteText.text = it.note
            noteTime.text =  DateFormat.format(dateFormatPattern, it.start)
        } ?: run {
            noteId = null
            notes = null
            noteText.text = loading
            noteTime.text = ""
        }
    }


}