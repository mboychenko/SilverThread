package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.htmlFromAssetsCompat

class PracticeFormattedDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.prectice_dialog_fragment, container, false)

        arguments?.let {
            it.getString(DIALOG_TITLE)?.let { title -> view.findViewById<TextView>(R.id.title).text = title }
            it.getString(DIALOG_TEXT)?.let { text ->
                view.findViewById<TextView>(R.id.text).text = htmlFromAssetsCompat(text)
            }
        }

        view.findViewById<View>(R.id.close).setOnClickListener { dismiss() }
        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        fun newInstance(title: String, text: String): PracticeFormattedDialog {
            val dialog = PracticeFormattedDialog()
            val args = Bundle().apply {
                putString(DIALOG_TITLE, title)
                putString(DIALOG_TEXT, text)
            }
            dialog.arguments = args
            return dialog
        }

        private const val DIALOG_TITLE = "DIALOG_TITLE"
        private const val DIALOG_TEXT = "DIALOG_TEXT"
        const val PRACTICES_DIALOG_TAG = "PRACTICES_DIALOG_TAG"
    }
}