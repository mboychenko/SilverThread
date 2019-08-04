package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.fromHtmlCompat

class PracticeFormattedDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.prectice_dialog_fragment, container, false)

        arguments?.getInt(DIALOG_TITLE_RES)?.let {
            view.findViewById<TextView>(R.id.title).text = getString(it)
        }
        arguments?.getInt(DIALOG_TEXT_RES)?.let {
            view.findViewById<TextView>(R.id.text).text = fromHtmlCompat(context, it)
        }

        view.findViewById<View>(R.id.close).setOnClickListener { dismiss() }
        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    companion object {
        fun newInstance(titleRes: Int, textRes: Int): PracticeFormattedDialog {
            val dialog = PracticeFormattedDialog()
            val args = Bundle().apply {
                putInt(DIALOG_TITLE_RES, titleRes)
                putInt(DIALOG_TEXT_RES, textRes)
            }
            dialog.arguments = args
            return dialog
        }

        private const val DIALOG_TITLE_RES = "DIALOG_TITLE"
        private const val DIALOG_TEXT_RES = "DIALOG_TEXT"
        const val PRACTICES_DIALOG_TAG = "PRACTICES_DIALOG_TAG"
    }
}