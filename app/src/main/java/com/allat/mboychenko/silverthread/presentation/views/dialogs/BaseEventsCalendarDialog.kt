package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.viewstate.BaseEventCalendarViewState
import com.applandeo.materialcalendarview.EventDay
import java.util.*

abstract class BaseEventsCalendarDialog<T : BaseEventCalendarViewState> : DialogFragment() {

    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView
    private lateinit var filterButton: TextView
    private lateinit var progress: ContentLoadingProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(getLayoutRes(), container, false)
        with(view) {
            calendarView = findViewById(R.id.calendar_view)
            progress = findViewById(R.id.progress)
            filterButton = findViewById(R.id.filter)
            calendarView.setMaximumDate(Calendar.getInstance())
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        filterButton.setOnClickListener {
            if (calendarView.selectedDates.size > 1) {
                filterAction(calendarView.selectedDates.first(), calendarView.selectedDates.last())
            } else if (calendarView.selectedDates.size == 1) {
                filterAction(calendarView.selectedDates.first())
            }
            dismiss()
        }
        calendarView.setMinimumDate(Calendar.getInstance())
    }

    protected open fun renderer(state: T) {
        if (state.loading) {
            progress.show()
        } else {
            progress.hide()
            state.data?.let { result ->

                result.lastOrNull()?.first?.let { calendarView.setMinimumDate(it) } ?: run {
                    Toast.makeText(context, getString(R.string.no_items_to_filter), Toast.LENGTH_LONG).show()
                }

                val events = mutableListOf<EventDay>()
                result.forEach { dayTimes ->
                    val (day, times) = dayTimes
                    val drawable: Int = when(times) {
                        1 -> R.drawable.ic_single_event
                        2 -> R.drawable.ic_two_events
                        else -> R.drawable.ic_multi_events
                    }
                    events.add(EventDay(day, drawable))
                }
                calendarView.setEvents(events)
            }
        }
    }

    @LayoutRes
    abstract fun getLayoutRes(): Int

    abstract fun filterAction(firstDate: Calendar, secondDate: Calendar? = null)

    protected fun clearSelection() {
        calendarView.selectedDates = emptyList()
    }

}