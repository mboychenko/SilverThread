package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.presentation.presenters.AllatPresenter
import kotlinx.android.synthetic.main.allat_fragment.*
import kotlinx.android.synthetic.main.allat_fragment.view.*
import org.koin.android.ext.android.inject

class AllatFragment: Fragment(), IAllatRaFragments, IAllatFragmentView {

    private val presenter: AllatPresenter by inject()
    private val notificationBeforeArray : IntArray by lazy { resources.getIntArray(R.array.notify_before_mins) }

    override fun getFragmentTag(): String = ALLAT_FRAGMENT_TAG

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.allat_fragment, container, false)
        fragment.btnGreenwich.setOnClickListener { presenter.setAllatTimeZone(AllatTimeZone.GMT) }
        fragment.btnKiev.setOnClickListener { presenter.setAllatTimeZone(AllatTimeZone.KIEV) }
        fragment.btnLocal.setOnClickListener { presenter.setAllatTimeZone(AllatTimeZone.LOCAL) }
        fragment.lockImg.setOnClickListener { lockUnlockConfig() }
        fragment.lockTitle.setOnClickListener { lockUnlockConfig() }

        fragment.ringOnStart.setOnCheckedChangeListener { _, isChecked ->  } //todo alarm ring on start
        fragment.ringOnEnd.setOnCheckedChangeListener { _, isChecked ->  } //todo alarm ring on end

        fragment.timezoneConfig.setOnClickListener {
            lockUnlockConfig(unlock = false)
            changeTimezoneSetupVisibility(true)
        }

        with(fragment.notifyTimer) {
            adapter = android.widget.ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                listOf(context.getString(R.string.none)) + notificationBeforeArray.map { it.toString() })
            setSelection(2)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, fragmentview: View?, position: Int, id: Long) {
                    if (position == 0) {    //None
                        // remove notifications
                        switchesState(false)
                    } else {
                        // setupNotifications before N mins
                        // notificationBeforeArray[position - 1]
                        switchesState(true)
                    }
                }
            }
        }

        return fragment
    }

    private fun switchesState(enable: Boolean) {
        if (enable.not()) {
            ringOnEnd?.isChecked = false
            ringOnStart?.isChecked = false
        }

        ringOnStart?.isEnabled = enable
        ringOnEnd?.isEnabled = enable
    }

    private fun lockUnlockConfig(unlock: Boolean? = null) {
        val locked = unlock ?: (configClickGrabber.visibility == View.VISIBLE)
        configClickGrabber.visibility = if (locked) View.GONE else View.VISIBLE
        lockTitle.text = getText(if (locked) R.string.click_to_lock else R.string.click_to_unlock)
        lockImg.setImageDrawable(ContextCompat.getDrawable(context!!,
            if (locked) R.drawable.ic_lock_open else R.drawable.ic_lock))
    }

    override fun updateTimer(h: Long, m: Long, s: Long) {
        clockTextView.text = String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun updateTimerStatus(allatStatusTitle: String) {
        allatTitle.text = allatStatusTitle
    }

    override fun changeTimezoneSetupVisibility(visible: Boolean) {
        timezoneContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        lockUnlockConfig(unlock = false)
        presenter.attachView(this)
    }
    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    companion object {
        const val ALLAT_FRAGMENT_TAG = "ALLAT_FRAGMENT_TAG"
    }
}
