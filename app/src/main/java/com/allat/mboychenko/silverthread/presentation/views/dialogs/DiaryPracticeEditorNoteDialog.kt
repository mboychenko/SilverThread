package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.observe
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.capitalizeEachNewWord
import com.allat.mboychenko.silverthread.presentation.viewmodels.PracticesDiaryViewModel
import com.allat.mboychenko.silverthread.presentation.viewmodels.IPracticeNotesEditorVM
import com.allat.mboychenko.silverthread.presentation.views.custom.LinedEditText
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


class DiaryPracticeEditorNoteDialog : DialogFragment() {

    private lateinit var startTimeView: TextView
    private lateinit var endTimeView: TextView
    private lateinit var practice: AutoCompleteTextView
    private lateinit var observations: LinedEditText
    private lateinit var save: TextView
    private lateinit var cancel: TextView

    private val practicesNotesViewModel: IPracticeNotesEditorVM by sharedViewModel<PracticesDiaryViewModel>(from = { parentFragment!! })

    private val dateFormatPattern: String by lazy {
        context?.getString(R.string.diary_date_format) ?: "dd MMM, yyyy 'at' HH:mm"
    }

    private var endTime: Calendar? by Delegates.observable(null, { _: KProperty<*>, _: Calendar?, new: Calendar? ->
        endTimeView.text = if (new == null) "" else DateFormat.format(dateFormatPattern, new)
    })
    private var startTime: Calendar? by Delegates.observable(null, { _: KProperty<*>, _: Calendar?, new: Calendar? ->
        startTimeView.text = if (new == null) "" else DateFormat.format(dateFormatPattern, new)
    })

    private var practiceGuid: String? = null
    private var uniqPracticesNames = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.practice_editor_view, container, false)
        with(view) {
            practice = findViewById(R.id.practice)
            startTimeView = findViewById(R.id.startTime)
            endTimeView = findViewById(R.id.endTime)
            observations = findViewById(R.id.observations)
            save = findViewById(R.id.save)
            cancel = findViewById(R.id.cancel)

            initListeners()

        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        practiceGuid = arguments?.getString(ARG_PRACTICE_GUID)

        practiceGuid?.let { guid ->
            practicesNotesViewModel.findPracticeByGuid(guid)?.let {
                practice.setText(it.title.capitalizeEachNewWord())
                startTime = it.startDate
                endTime = it.endDate
                observations.setText(it.note)
            }
        }

        context?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_list_item_1,
                uniqPracticesNames
            ).also { adapter ->
                practice.setAdapter(adapter)
                practice.threshold = 1
            }
        }

        practicesNotesViewModel.bindUniqPracticesNames().observe(viewLifecycleOwner, onChanged = {
            uniqPracticesNames.clear()
            uniqPracticesNames.addAll(it.map { word -> word.capitalizeEachNewWord() })
        })


    }

    private fun initListeners() {
        startTimeView.setOnClickListener {
            showDateTimePickerDialog(
                childFragmentManager,
                endLimit = Calendar.getInstance().timeInMillis + TimeUnit.MINUTES.toMillis(5)
            ) { pickedDateTime ->
                startTime = pickedDateTime
            }
        }

        endTimeView.setOnClickListener {
            if (startTime != null) {
                showDateTimePickerDialog(
                    childFragmentManager,
                    startLimit = startTime?.timeInMillis,
                    endLimit = Calendar.getInstance().timeInMillis + TimeUnit.MINUTES.toMillis(5)
                ) { pickedDateTime ->
                    endTime = pickedDateTime
                }
            } else {
                Toast.makeText(context, getString(R.string.start_time_first), Toast.LENGTH_LONG).show()
            }
        }

        cancel.setOnClickListener { dismiss() }
        save.setOnClickListener {
            if (practice.text.isEmpty() || startTime == null) {
                Toast.makeText(context, getString(R.string.practice_and_time_mandatory), Toast.LENGTH_LONG).show()
            } else {
                //save
                practiceGuid?.let {
                    practicesNotesViewModel.updatePractice(
                        it,
                        getPractice(),
                        startTime!!,
                        endTime,
                        getObservations()
                    )
                } ?: run {
                    practicesNotesViewModel.addPractice(
                        getPractice(),
                        startTime!!,
                        endTime,
                        getObservations()
                    )
                }
                dismiss()
            }
        }
    }

    private fun getObservations() =
        if (observations.text?.isNotEmpty() == true) observations.text.toString() else null

    private fun getPractice() = practice.text.toString()

    companion object {
        const val DIARY_PRACTICE_EDITOR_DIALOG_TAG = "DIARY_PRACTICE_EDITOR_DIALOG_TAG"

        private const val ARG_PRACTICE_GUID = "arg_practice_guid"

        fun getInstance(guid: String? = null): DiaryPracticeEditorNoteDialog {
            val dialog = DiaryPracticeEditorNoteDialog()
            val args = Bundle()
            args.putString(ARG_PRACTICE_GUID, guid)
            dialog.arguments = args
            return dialog
        }
    }

}