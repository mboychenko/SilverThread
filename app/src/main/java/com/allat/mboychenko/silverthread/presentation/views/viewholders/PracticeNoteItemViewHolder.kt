package com.allat.mboychenko.silverthread.presentation.views.viewholders

import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.capitalizeEachNewWord
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeNoteItem
import java.util.concurrent.TimeUnit

class PracticeNoteItemViewHolder(
    parentView: ViewGroup,
    actionListener: INoteActionListener,
    layoutId: Int
) : DiaryBaseViewHolder<PracticeNoteItem>(parentView, actionListener, layoutId) {

    private var notes: String? = null

    private var duration: TextView
    private var durationGroup: Group
    private var startTime: TextView
    private var title: TextView
    private var notesImg: ImageView

    private val inProgressText: String by lazy { itemView.context.getString(R.string.in_progress) }

    init {
        with(itemView){
            duration = findViewById(R.id.duration)
            durationGroup = findViewById(R.id.duration_group)
            startTime = findViewById(R.id.start_time)
            title = findViewById(R.id.title_value)
            notesImg = findViewById(R.id.notes)

            setOnClickListener {
                if (!notes.isNullOrEmpty()) {
                    AlertDialog.Builder(itemView.context)
                        .setTitle(R.string.observations_title)
                        .setMessage(notes)
                        .setNegativeButton(parentView.context.getString(R.string.hide)) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }

            findViewById<ImageView>(R.id.button_more).apply{
                initPopup(this)
                setOnClickListener { menuPopupHelper.show() }
            }
        }
    }

    override fun bindTo(note: PracticeNoteItem?) {
        note?.let {
            noteId = it.noteId
            notes = it.note
            title.text = it.title.capitalizeEachNewWord()
            startTime.text =  DateFormat.format(dateFormatPattern, it.startDate)

            if (notes?.isNotEmpty() == true) {
                notesImg.visibility = View.VISIBLE
            } else {
                notesImg.visibility = View.INVISIBLE
            }

            if (it.duration == 0L) {
                duration.text = inProgressText
            } else {
                duration.text = itemView.context.getString(R.string.mins, TimeUnit.MILLISECONDS.toMinutes(it.duration))
            }
        }

    }
}