package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.htmlFromAssetsCompat
import com.allat.mboychenko.silverthread.presentation.views.fragments.IViewContext

class PracticeFormattedDialog : DialogFragment(), IViewContext {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        val view = inflater.inflate(R.layout.prectice_dialog_fragment, container, false)

        var practiceTitle: Spannable? = null
        var practice: Spannable? = null

        arguments?.let {
            it.getString(DIALOG_TITLE)?.let { title ->
                view.findViewById<TextView>(R.id.title).text = title
                practiceTitle = htmlFromAssetsCompat("<p style=\"text-align:center\"><b>$title</b></p>")
            }
            it.getString(DIALOG_TEXT)?.let { text ->
                practice = htmlFromAssetsCompat(text)
                view.findViewById<TextView>(R.id.text).text = practice
            }
        }

        val sharedText = StringBuilder().append(practiceTitle.toString()).append(practice.toString())

        view.findViewById<View>(R.id.share).setOnClickListener {
            shareText(sharedText.toString(), getString(R.string.share_practice))
        }
        view.findViewById<View>(R.id.close).setOnClickListener { dismiss() }
        return view
    }

    override fun getViewContext(): Context? {
        return context
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