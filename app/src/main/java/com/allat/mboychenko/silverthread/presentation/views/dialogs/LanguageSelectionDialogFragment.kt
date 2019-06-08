package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.BooksConstants
import java.util.ArrayList

class LanguageSelectionDialogFragment : DialogFragment() {

    private val locales = mutableListOf<BooksConstants.BooksLocale>()
    private var title: String = ""
    private var listener: LanguageSelectedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        getArgs(arguments)
        return AlertDialog.Builder(context!!)
            .setAdapter(ArrayAdapter<BooksConstants.BooksLocale>(context!!,
                R.layout.dialog_language_line, locales)
            ) { _, which ->  listener?.onSelected(locales[which]) }

            .setTitle(title)
            .setOnCancelListener { dialog -> dialog.dismiss() }
            .create()
    }

    private fun getArgs(bundle: Bundle?) {
        title = bundle?.getString(DIALOG_TITLE) ?: ""
        val localesOrdinal = bundle?.getIntegerArrayList(DIALOG_LOCALES_LIST)
        localesOrdinal?.let {
            for (ordinal in it) {
                locales.add(BooksConstants.BooksLocale.values()[ordinal])
            }
        }
    }

    fun setListener(listener: LanguageSelectedListener) {
        this.listener = listener
    }

    interface LanguageSelectedListener {
        fun onSelected(locale: BooksConstants.BooksLocale)
    }

    companion object {
        fun newInstance(title: String, languages: List<BooksConstants.BooksLocale>): LanguageSelectionDialogFragment {
            val dialog = LanguageSelectionDialogFragment()
            val args = Bundle().apply {
                putIntegerArrayList(DIALOG_LOCALES_LIST, languages.map { it.ordinal } as ArrayList<Int>)
                putString(DIALOG_TITLE, title)
            }
            dialog.arguments = args
            return dialog
        }

        private const val DIALOG_TITLE = "DIALOG_TITLE"
        private const val DIALOG_LOCALES_LIST = "DIALOG_LOCALES_LIST"
        const val LANGUAGE_SELECTOR_DIALOG_TAG = "LANGUAGE_SELECTOR_DIALOG_TAG"
    }

}