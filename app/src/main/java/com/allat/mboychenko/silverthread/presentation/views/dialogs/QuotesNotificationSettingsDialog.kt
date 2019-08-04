package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesNotificationSettingsPresenter
import kotlinx.android.synthetic.main.quotes_notification_settings_dialog.view.*
import org.koin.android.ext.android.inject

class QuotesNotificationSettingsDialog : DialogFragment(), IQuotesNotificationSettingView {

    private val presenter: QuotesNotificationSettingsPresenter by inject()
    private val randomQuotesTimesArray : IntArray by lazy { resources.getIntArray(R.array.random_quotes_times) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.quotes_notification_settings_dialog, container, false)

        with(view.quoteNum) {
            adapter = ArrayAdapter<Int>(context, R.layout.spinner_dropdown_item, randomQuotesTimesArray.asList())

            val times = presenter.getRandomQuoteTimesInDay()            //setup selected
            randomQuotesTimesArray.indexOf(times)
                .takeIf { it != -1 }
                ?.let { setSelection(it) }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedTimes = presenter.getRandomQuoteTimesInDay()            //setup selected
                    val newValue = randomQuotesTimesArray[position]
                    if (selectedTimes != newValue) {    //Disabled
                        presenter.setRandomQuoteTimesInDay(newValue)
                    }
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    companion object {
        const val QUOTES_NOTIF_SETTINGS_DIALOG_TAG = "QUOTES_NOTIF_SETTINGS_DIALOG_TAG"
    }
}