package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import androidx.lifecycle.observe
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.intents.DiaryAction
import com.allat.mboychenko.silverthread.presentation.viewmodels.DiaryNotesViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.IDiaryNotesCalendarViewModel
import com.allat.mboychenko.silverthread.presentation.viewstate.BaseEventCalendarViewState
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class DiaryNotesCalendarDialog : BaseEventsCalendarDialog<BaseEventCalendarViewState>() {

    private val viewModel: IDiaryNotesCalendarViewModel by sharedViewModel<DiaryNotesViewModel>(from = { parentFragment!! })

    override fun getLayoutRes() = R.layout.diary_calendar_dialog

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.bindCalendar().observe(viewLifecycleOwner, ::renderer)
        viewModel.intentCalendar(DiaryAction.Load)
    }
    override fun filterAction(firstDate: Calendar, secondDate: Calendar?) {
        viewModel.intentCalendar(DiaryAction.FilterDate(Pair(firstDate, secondDate)))
    }

    companion object {
        const val DIARY_CALENDAR_DIALOG_TAG = "DIARY_CALENDAR_DIALOG_TAG"
    }
}