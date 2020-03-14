package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.*
import androidx.lifecycle.observe
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeNoteItem
import com.allat.mboychenko.silverthread.presentation.viewmodels.*
import com.allat.mboychenko.silverthread.presentation.views.adapters.PracticeNoteAdapter
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryPracticeEditorNoteDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryPracticeEditorNoteDialog.Companion.DIARY_PRACTICE_EDITOR_DIALOG_TAG
import com.allat.mboychenko.silverthread.presentation.views.dialogs.PracticeNotesCalendarDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.PracticeNotesCalendarDialog.Companion.NOTES_CALENDAR_DIALOG_TAG
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener
import com.allat.mboychenko.silverthread.presentation.views.viewholders.PracticeNoteItemViewHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import android.view.ViewGroup
import android.content.Context
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.core.widget.ContentLoadingProgressBar
import com.allat.mboychenko.silverthread.presentation.intents.DiaryAction
import com.allat.mboychenko.silverthread.presentation.models.PracticesSort
import com.allat.mboychenko.silverthread.presentation.views.custom.PracticeFilterSpinner
import com.allat.mboychenko.silverthread.presentation.viewstate.PracticeViewState
import java.security.KeyStore
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


class DiaryPracticesFragment : BaseSearchFragment(), IViewContext {

    override fun getViewContext() = context

    private val notesViewModel: IPracticeNotesBaseViewModel by sharedViewModel<PracticesDiaryViewModel>(
        from = { this })

    private lateinit var filter: ImageView
    private lateinit var progress: ContentLoadingProgressBar
    private lateinit var cancelFilterSort: ImageView
    private lateinit var cancelDateFilter: ImageView
    private lateinit var dateFilterGroup: Group
    private lateinit var emptyItemsPlaceholder: TextView
    private lateinit var dateFilter: TextView
    private lateinit var sortSpinner: Spinner
    private lateinit var filterSpinner: PracticeFilterSpinner
    private lateinit var notesList: RecyclerView
    private lateinit var notesAdapter: PagedListAdapter<PracticeNoteItem, PracticeNoteItemViewHolder>
    private val sortAdapter by lazy { SortSpinAdapter(context!!) }

    private var dayFilter: Pair<Calendar, Calendar?>? by Delegates.observable(null, { _: KProperty<*>, old: Pair<Calendar, Calendar?>?, new: Pair<Calendar, Calendar?>? ->
        if (new != old) {
            if (new != null) {
                dateFilterGroup.visibility = View.VISIBLE
                dateFilter.text = datesFilterRangeToString(new)
            } else {
                dateFilterGroup.visibility = View.GONE
                dateFilter.text = ""
            }
        }
    })

    private var practiceFilter: String? by Delegates.observable(null, { _: KProperty<*>, old: String?, new: String? ->
        if (new != old) {
            notesViewModel.intent(DiaryAction.FilterPractice(title = new))
        }
    })

    private var sortState: PracticesSort by Delegates.observable(
        PracticesSort.TIME, { _: KProperty<*>, old: PracticesSort, new: PracticesSort ->
        if (new != old) {
            notesViewModel.intent(DiaryAction.Sort(new))
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.practices_notes_fragment, container, false)
        notesList = view.findViewById(R.id.notes_list)
        filter = view.findViewById(R.id.filter_icon)
        sortSpinner = view.findViewById(R.id.sort)
        filterSpinner = view.findViewById(R.id.filter)
        dateFilterGroup = view.findViewById(R.id.data_filter_group)
        cancelDateFilter = view.findViewById(R.id.cancel_date_filter)
        cancelFilterSort = view.findViewById(R.id.cancel_filter_sort)
        dateFilter = view.findViewById(R.id.date_filter)
        progress = view.findViewById(R.id.progress)
        emptyItemsPlaceholder = view.findViewById(R.id.empty_items_placeholder)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<View>(R.id.sort_click_grabber)?.setOnClickListener {
            sortSpinner.performClick()
        }
        view?.findViewById<View>(R.id.filter_click_grabber)?.setOnClickListener {
            filterSpinner.performClick()
        }

        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = sortSelectionListener

        filterSpinner.setAdapter(ArrayAdapter<String>(context, R.layout.sort_spin_item).apply {
            setDropDownViewResource(R.layout.sort_spin_dropdown_item)
        })
        filterSpinner.setSelectionCallback { practiceFilter = it }

        cancelDateFilter.setOnClickListener { notesViewModel.intent(DiaryAction.ClearDateFilter) }
        cancelFilterSort.setOnClickListener { notesViewModel.intent(DiaryAction.ClearFilterSortSelections) }

        with(notesList) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            notesAdapter = PracticeNoteAdapter(noteActionListener)
            adapter = notesAdapter
        }
        notesViewModel.bind().observe(viewLifecycleOwner, ::renderer)
    }

    private val noteActionListener = object : INoteActionListener {
        override fun onEdit(id: String) {
            DiaryPracticeEditorNoteDialog.getInstance(id).show(childFragmentManager, DIARY_PRACTICE_EDITOR_DIALOG_TAG)
        }

        override fun onDelete(id: String) {
            notesViewModel.removePractice(id)
        }
    }

    private fun renderer(state: PracticeViewState) {
        dayFilter = state.filterDate

        if (state.sortState != sortState) {
            sortSpinner.onItemSelectedListener = null
            sortSpinner.setSelection(sortAdapter.getPosition(state.sortState))
            sortSpinner.onItemSelectedListener = sortSelectionListener
        }

        state.uniqPracticeNames?.let {
            filterSpinner.updateItems(it, state.filterPractice)
        } ?: run {
            filterSpinner.placeHolder()
        }

        if (state.filterPractice != null || state.sortState != PracticesSort.TIME) {
            cancelFilterSort.visibility = View.VISIBLE
        } else {
            cancelFilterSort.visibility = View.GONE
        }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.calendar -> {
                PracticeNotesCalendarDialog().show(childFragmentManager, NOTES_CALENDAR_DIALOG_TAG)
                true
            }
            else -> false
        }
    }

    override fun search(text: String) {
        notesViewModel.intent(DiaryAction.Search(text))
    }

    private val sortSelectionListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            sortState = sortAdapter.getItem(position) ?: PracticesSort.TIME
        }

    }

    inner class SortSpinAdapter(
        context: Context
    ) : ArrayAdapter<PracticesSort>(context, R.layout.sort_spin_item, PracticesSort.values()) {

        init {
            setDropDownViewResource(R.layout.sort_spin_dropdown_item)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val label = super.getView(position, convertView, parent) as TextView
            getItem(position)?.resId?.let { label.text = context.getString(it) }
            return label
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val label = super.getDropDownView(position, convertView, parent) as TextView
            getItem(position)?.resId?.let { label.text = context.getString(it) }
            return label
        }

    }

}