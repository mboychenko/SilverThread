package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.viewmodels.DiaryNotesViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.IDiaryDialogEditor
import com.allat.mboychenko.silverthread.presentation.views.custom.LinedEditText
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class DiaryNoteEditorDialog : DialogFragment() {

    private val notesViewModel: IDiaryDialogEditor by sharedViewModel<DiaryNotesViewModel>(from = { requireParentFragment() })

    private var noteGuid: String? = null

    private lateinit var note: LinedEditText
    private lateinit var whenTime: TextView

    private val dateFormatPattern: String by lazy {
        context?.getString(R.string.diary_date_format) ?: "dd MMM, yyyy 'at' HH:mm"
    }

    private var noteTime: Calendar? by Delegates.observable(null, { _: KProperty<*>, _: Calendar?, new: Calendar? ->
        whenTime.text = if (new == null) "" else DateFormat.format(dateFormatPattern, new)
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.note_editor_view, container, false)
        with(view) {
            note = findViewById(R.id.note)
            whenTime = findViewById<TextView>(R.id.when_time).apply {
                setOnClickListener {
                    showDateTimePickerDialog(
                        childFragmentManager,
                        endLimit = Calendar.getInstance().timeInMillis + TimeUnit.MINUTES.toMillis(5)
                    ) { pickedDateTime ->
                        noteTime = pickedDateTime
                    }
                }
            }

            findViewById<TextView>(R.id.cancel).setOnClickListener { dismiss() }
            findViewById<TextView>(R.id.save).setOnClickListener {
                if (getNotes() == null || noteTime == null) {
                    Toast.makeText(context, context.getString(R.string.time_note_mandatory), Toast.LENGTH_LONG).show()
                } else {
                    //save
                    noteGuid?.let {
                        notesViewModel.updateNote(it, noteTime!!, getNotes()!!)
                    } ?: run {
                        notesViewModel.addNote(noteTime!!, getNotes()!!)
                    }
                    dismiss()
                }
            }
        }
        return view
    }

    private fun getNotes(): String? {
        return if (note.text?.isNotEmpty() == true) note.text.toString() else null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        noteGuid = arguments?.getString(ARG_NOTE_GUID)?.also { guid ->
            notesViewModel.getNote(guid)?.let {
                note.setText(it.note)
                noteTime = it.start
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    companion object {
        const val DIARY_NOTE_EDITOR_DIALOG_TAG = "DIARY_NOTE_EDITOR_DIALOG_TAG"

        private const val ARG_NOTE_GUID = "arg_note_guid"

        fun getInstance(guid: String? = null): DiaryNoteEditorDialog {
            val dialog = DiaryNoteEditorDialog()
            val args = Bundle()
            args.putString(ARG_NOTE_GUID, guid)
            dialog.arguments = args
            return dialog
        }
    }

}