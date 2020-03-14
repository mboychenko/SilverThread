package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.allat.mboychenko.silverthread.R
import java.util.*

class DateTimePickerDialog : DialogFragment() {

    private lateinit var root: ConstraintLayout
    private lateinit var timePicker: TimePicker
    private lateinit var datePicker: DatePicker
    private lateinit var selectedDate: TextView
    private lateinit var confirmText: TextView

    private val save: String by lazy { context?.getString(R.string.save) ?: "" }
    private val confirm: String by lazy { context?.getString(R.string.confirm) ?: "" }

    private val constraintDate = ConstraintSet()
    private val constraintTime = ConstraintSet()

    var dateTimeSelectedCallback: ((Calendar) -> Unit)? = null

    private var viewState = DateTimePickerViewState.DATE

    private enum class DateTimePickerViewState {
        DATE,
        TIME
    }

    private val pickedDateTime = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.date_time_picker_date_stage, container, false)

        with(view) {
            root = findViewById(R.id.root)
            selectedDate = findViewById(R.id.date_value)
            confirmText = findViewById(R.id.confirm_text)

            findViewById<CardView>(R.id.confirm).setOnClickListener(setDateTimeOnClickListener)
            findViewById<CardView>(R.id.back).setOnClickListener(editDateOnClickListener)

            timePicker = findViewById(R.id.time_picker)
            timePicker.setIs24HourView(true)

            datePicker = findViewById(R.id.date_picker)

            setupCompatibility()

            initConstraints()
        }


        return view
    }

    private fun setupCompatibility() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            datePicker.calendarViewShown = false
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { dismissAllowingStateLoss() }
    }

    private var startLim: Long = 0
    private var endLim: Long = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            startLim = it.getLong(START_LIM_ARG)
            endLim = it.getLong(END_LIM_ARG)
            if (startLim > 0) datePicker.minDate = startLim
            if (endLim > 0) datePicker.maxDate = endLim
        }
    }

    private fun initConstraints() {
        constraintDate.clone(root)
        constraintTime.clone(context, R.layout.date_time_picker_time_stage)
    }

    private val editDateOnClickListener = View.OnClickListener {
        if (viewState != DateTimePickerViewState.DATE) {
            viewState = DateTimePickerViewState.DATE
            TransitionManager.beginDelayedTransition(root as ViewGroup, animatedTransition)
            constraintDate.applyTo(root)
        }
    }

    private val animatedTransition = ChangeBounds().apply {
        addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                confirmText.visibility = View.VISIBLE
            }

            override fun onTransitionResume(transition: Transition) {}

            override fun onTransitionPause(transition: Transition) {}

            override fun onTransitionCancel(transition: Transition) {}

            override fun onTransitionStart(transition: Transition) {
                confirmText.visibility = View.INVISIBLE
                confirmText.text = if (viewState != DateTimePickerViewState.DATE) save else confirm
            }

        })
    }

    @Suppress("DEPRECATION")
    private val setDateTimeOnClickListener = View.OnClickListener {
        if (viewState != DateTimePickerViewState.TIME) {
            pickedDateTime.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth
            )

            selectedDate.text = getString(R.string.selected_date, DateFormat.format(DATE_FORMAT_PATTERN, pickedDateTime))

            viewState = DateTimePickerViewState.TIME
            TransitionManager.beginDelayedTransition(root as ViewGroup, animatedTransition)
            constraintTime.applyTo(root)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pickedDateTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                pickedDateTime.set(Calendar.MINUTE, timePicker.minute)
            } else {
                pickedDateTime.set(Calendar.HOUR_OF_DAY, timePicker.currentHour)
                pickedDateTime.set(Calendar.MINUTE, timePicker.currentMinute)
            }

            if (startLim > 0 && pickedDateTime.timeInMillis < startLim) {
                Toast.makeText(context, getString(R.string.time_cant_be_earlier_then, DateFormat.format(DATE_TIME_FORMAT_PATTERN, startLim)), Toast.LENGTH_LONG).show()
            } else if(endLim > 0 && pickedDateTime.timeInMillis > endLim) {
                Toast.makeText(context, getString(R.string.selected_time_cant_be_before_now), Toast.LENGTH_LONG).show()
            } else {
                dateTimeSelectedCallback?.invoke(pickedDateTime)
                dismissAllowingStateLoss()
            }

        }
    }

    companion object {
        const val DATE_TIME_PICKER_DIALOG_TAG = "DATE_TIME_PICKER_DIALOG_TAG"
        private const val DATE_FORMAT_PATTERN = "dd MMM, yyyy"
        private const val DATE_TIME_FORMAT_PATTERN = "HH:MM dd MMM, yyyy"

        private const val START_LIM_ARG = "START_LIM_ARG"
        private const val END_LIM_ARG = "END_LIM_ARG"

        fun newInstance(startLimit: Long? = null, endLimit: Long? = null): DateTimePickerDialog {
            val dialog = DateTimePickerDialog()
            dialog.arguments = Bundle().apply {
                startLimit?.let { putLong(START_LIM_ARG, it) }
                endLimit?.let { putLong(END_LIM_ARG, it) }
            }
            return dialog
        }

    }
}