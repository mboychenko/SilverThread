package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.presentation.helpers.AlarmNotificationCodes
import com.allat.mboychenko.silverthread.presentation.presenters.AllatPresenter
import kotlinx.android.synthetic.main.allat_fragment.*
import kotlinx.android.synthetic.main.allat_fragment.view.*
import org.koin.android.ext.android.inject

class AllatFragment: BaseAllatRaFragment(), IAllatFragmentView {

    private val presenter: AllatPresenter by inject()
    private val notificationMinutesBeforeArray : IntArray by lazy { resources.getIntArray(R.array.notify_before_mins) }

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

        fragment.timezoneConfig.setOnClickListener {
            lockUnlockConfig(unlock = false)
            changeTimezoneSetupVisibility(true)
        }

        with(fragment.notifyTimer) {
            adapter = android.widget.ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                listOf(context.getString(R.string.none)) + notificationMinutesBeforeArray.map { it.toString() })
        }

        return fragment
    }

    override fun toolbarTitle(): Int = R.string.allat

    private fun lockUnlockConfig(unlock: Boolean? = null) {
        val locked = unlock ?: (configClickGrabber.visibility == View.VISIBLE)
        configClickGrabber.visibility = if (locked) View.GONE else View.VISIBLE
        lockTitle.text = getText(if (locked) R.string.click_to_lock else R.string.click_to_unlock)
        lockImg.setImageDrawable(ContextCompat.getDrawable(context!!,
            if (locked) R.drawable.ic_lock_open else R.drawable.ic_lock))
    }

    override fun updateAllatTimer(h: Long, m: Long, s: Long) {
        clockTextView.text = String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun updateVerdictTimer(days: Int, hoursLeft: Int, minsLeft: Int) {
        verdictClock.text = String.format(
            "%s %s %s",
            resources.getQuantityString(R.plurals.days_to, days, days),
            resources.getQuantityString(R.plurals.hours_to, hoursLeft, hoursLeft),
            resources.getQuantityString(R.plurals.minutes_to, minsLeft, minsLeft)
        )
    }

    override fun updateVerdictTitle(title: String) {
        verdictTitle.text = title
    }

    override fun updateVerdictSubtitle(subtitle: String) {
        verdictSubtitle.text = subtitle
    }

    override fun updateTimerStatus(allatStatusTitle: String) {
        allatTitle.text = allatStatusTitle
    }

    override fun changeTimezoneSetupVisibility(visible: Boolean) {
        timezoneContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun openDrawer() {
        getDrawer(activity)?.openDrawer(GravityCompat.START)
    }

    override fun ringOnStartEnabled(enabled: Boolean) {
        ringOnStart.isChecked = enabled
    }

    override fun ringOnEndEnabled(enabled: Boolean) {
        ringOnEnd.isChecked = enabled
    }

    override fun setAllatSoundLoud(loud: Boolean) {
        ringLevel.isChecked = loud
    }

    override fun initAlarmsCheckboxListeners() {
        ringOnStart.setOnCheckedChangeListener { _, enabled ->
            presenter.startStopAlarm(AlarmNotificationCodes.ALLAT_START, enabled)
        }
        ringOnEnd.setOnCheckedChangeListener { _, enabled ->
            presenter.startStopAlarm(AlarmNotificationCodes.ALLAT_END, enabled)
        }
        ringLevel.setOnCheckedChangeListener { _, loud ->
            presenter.setRingLevel(loud)
        }
    }

    override fun allatNotifIn(minutes: Int) {
        if (minutes != -1) {
            notificationMinutesBeforeArray.indexOf(minutes)
                .takeIf { it != -1 }
                ?.let { notifyTimer.setSelection(it + 1) }
        }

        notifyTimer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, fragmentview: View?, position: Int, id: Long) {
                presenter.allatNotifInSelected(position)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lockUnlockConfig(unlock = false)
        presenter.attachView(this)

    }

    override fun onStop() {
        super.onStop()
        presenter.detachView()
    }

    companion object {
        const val ALLAT_FRAGMENT_TAG = "ALLAT_FRAGMENT_TAG"
    }
}
