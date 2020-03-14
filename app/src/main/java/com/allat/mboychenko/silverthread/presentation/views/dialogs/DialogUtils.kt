package com.allat.mboychenko.silverthread.presentation.views.dialogs

import java.util.*
import androidx.fragment.app.FragmentManager
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DateTimePickerDialog.Companion.DATE_TIME_PICKER_DIALOG_TAG

fun showDateTimePickerDialog(
    fragmentManager: FragmentManager,
    startLimit: Long? = null,
    endLimit: Long? = null,
    callback: (calendar: Calendar) -> Unit
) {
    DateTimePickerDialog.newInstance(startLimit, endLimit)
        .apply { dateTimeSelectedCallback = callback }
        .show(fragmentManager, DATE_TIME_PICKER_DIALOG_TAG)
}
