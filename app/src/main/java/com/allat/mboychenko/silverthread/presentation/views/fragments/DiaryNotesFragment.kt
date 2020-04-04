package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.observe
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.intents.DiaryAction
import com.allat.mboychenko.silverthread.presentation.views.listitems.DiaryNoteItem
import com.allat.mboychenko.silverthread.presentation.viewmodels.DiaryNotesViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.IDiaryNotes
import com.allat.mboychenko.silverthread.presentation.views.adapters.DiaryNoteAdapter
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryNoteEditorDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryNoteEditorDialog.Companion.DIARY_NOTE_EDITOR_DIALOG_TAG
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryNotesCalendarDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryNotesCalendarDialog.Companion.DIARY_CALENDAR_DIALOG_TAG
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener
import com.allat.mboychenko.silverthread.presentation.views.viewholders.DiaryNoteViewHolder
import com.allat.mboychenko.silverthread.presentation.viewstate.DiaryViewState
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class DiaryNotesFragment : BaseDiarySearchFragment(), IViewContext {

    private val notesViewModel: IDiaryNotes by sharedViewModel<DiaryNotesViewModel>(from = { this })

    private lateinit var notesList: RecyclerView
    private lateinit var dataFilterGroup: Group
    private lateinit var removeFilter: ImageView
    private lateinit var dateFilterTitle: TextView
    private lateinit var progress: ContentLoadingProgressBar
    private lateinit var emptyItemsPlaceholder: TextView
    private lateinit var notesAdapter: PagedListAdapter<DiaryNoteItem, DiaryNoteViewHolder>

    private var dayFilter: Pair<Calendar, Calendar?>? by Delegates.observable(null, { _: KProperty<*>, old: Pair<Calendar, Calendar?>?, new: Pair<Calendar, Calendar?>? ->
        if (new != old) {
            if (new != null) {
                dataFilterGroup.visibility = View.VISIBLE
                dateFilterTitle.text = datesFilterRangeToString(new)
            } else {
                dataFilterGroup.visibility = View.GONE
                dateFilterTitle.text = ""
            }
        }
    })

    override fun getViewContext() = context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_notes_fragment, container, false)
        notesList = view.findViewById(R.id.notes_list)
        dataFilterGroup = view.findViewById(R.id.date_group)
        removeFilter = view.findViewById(R.id.cancel_date_filter)
        dateFilterTitle = view.findViewById(R.id.date_filter)
        progress = view.findViewById(R.id.progress)
        emptyItemsPlaceholder = view.findViewById(R.id.empty_items_placeholder)

        removeFilter.setOnClickListener { notesViewModel.intent(DiaryAction.ClearDateFilter) }

        with(notesList) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            notesAdapter = DiaryNoteAdapter(noteActionListener)
            adapter = notesAdapter
        }

        return view
    }

    private val noteActionListener = object : INoteActionListener {
        override fun onEdit(id: String) {
            DiaryNoteEditorDialog.getInstance(id).show(childFragmentManager, DIARY_NOTE_EDITOR_DIALOG_TAG)
        }

        override fun onDelete(id: String) {
            notesViewModel.removeNote(id)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.calendar -> {
                DiaryNotesCalendarDialog().show(childFragmentManager, DIARY_CALENDAR_DIALOG_TAG)
                true
            }
            else -> false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        notesViewModel.bind().observe(viewLifecycleOwner, ::renderer)
    }

    private fun renderer(state: DiaryViewState) {
        dayFilter = state.filterDate

        if (state.loading) {
            progress.show()
        } else {
            notesAdapter.submitList(state.data) {
                progress.hide()
                emptyItemsPlaceholder.visibility = if (notesAdapter.currentList.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

        }
    }

    override fun search(text: String) {
        notesViewModel.intent(DiaryAction.Search(text))
    }

}