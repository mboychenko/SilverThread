package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.observe
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.intents.DiaryAction
import com.allat.mboychenko.silverthread.presentation.viewmodels.PracticesDiaryViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.IPracticesDiaryCalendarViewModel
import com.allat.mboychenko.silverthread.presentation.views.custom.PracticeFilterSpinner
import com.allat.mboychenko.silverthread.presentation.viewstate.PracticeCalendarViewState
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class PracticeNotesCalendarDialog : BaseEventsCalendarDialog<PracticeCalendarViewState>() {

    private lateinit var filterSpinner: PracticeFilterSpinner

    private val notesViewModel: IPracticesDiaryCalendarViewModel by sharedViewModel<PracticesDiaryViewModel>(from = { parentFragment!! })

    override fun getLayoutRes(): Int = R.layout.notes_calendar_dialog

    private var practiceFilter: String? by Delegates.observable(null, { _: KProperty<*>, old: String?, new: String? ->
        if (new != old) {
            clearSelection()
            notesViewModel.intentCalendar(DiaryAction.FilterPractice(title = new))
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filterSpinner = view.findViewById(R.id.spinner)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let {
            filterSpinner.setAdapter(ArrayAdapter<String>(it, R.layout.calendar_filter_spin_item).apply {
                setDropDownViewResource(R.layout.sort_spin_dropdown_item)
            })
        }

        filterSpinner.setSelectionCallback { practiceFilter = it }

        notesViewModel.bindCalendar().observe(viewLifecycleOwner, ::renderer)
        notesViewModel.intentCalendar(DiaryAction.Load)
    }

    override fun filterAction(firstDate: Calendar, secondDate: Calendar?) {
        notesViewModel.intentCalendar(DiaryAction.FilterDatePractice(practiceFilter, Pair(firstDate, secondDate)))
    }

    override fun renderer(state: PracticeCalendarViewState) {
        super.renderer(state)
        state.uniqPracticeNames?.let {
            filterSpinner.updateItems(it, state.filterPractice)
        }
    }

    companion object {
        const val NOTES_CALENDAR_DIALOG_TAG = "NOTES_CALENDAR_DIALOG_TAG"
    }

}