package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import android.os.CountDownTimer
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneStorage
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper.TimeStatus.*
import com.allat.mboychenko.silverthread.presentation.views.fragments.IAllatFragmentView
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class AllatPresenter(private val context: Context, private val storage: AllatTimeZoneStorage) : BasePresenter<IAllatFragmentView>(){

    private val allatStatusAwait : String by lazy { context.getString(R.string.time_to_group_meditation) }
    private val allatStatusOngoing : String by lazy { context.getString(R.string.allat_started_meditation) }

    private var timerStatus: AllatHelper.TimeStatus by Delegates.observable(INIT) {
            _, old, new ->
        if (old != new) {
            view?.updateTimerStatus(if (new == AWAITING) allatStatusAwait else allatStatusOngoing)
        }
    }

    private var timerTask: CountDownTimer? = null

    override fun attachView(view: IAllatFragmentView) {
        super.attachView(view)
        val timezone = storage.getAllatTimezone()
        if (timezone != AllatTimeZone.NOT_INIT) {
            view.changeTimezoneSetupVisibility(false)
            startTimer(timezone)
        } else {
            view.changeTimezoneSetupVisibility(true)
        }
    }

    override fun detachView() {
        super.detachView()
        timerTask?.cancel()
    }

    fun setAllatNotifIn(mins: Int) {
        storage.putAllatNotificationBefore(mins)
        //todo setup notification
    }

    fun removeAllatNotif() {
        storage.removeAllatNotification()
        //todo cancel notifications
    }

    fun getAllatNotifIn(): Int =
        storage.getAllatNotificationBeforeMins()


    fun setAllatTimeZone(timezone: AllatTimeZone) {
        storage.putAllatTimezone(timezone)
        view?.let {
            it.changeTimezoneSetupVisibility(false)
            startTimer(timezone)
        }
    }

    private fun startTimer(timezone: AllatTimeZone) {
        timerTask?.cancel()

        val (time, status) = AllatHelper.getTimeToAllat(timezone)
        timerStatus = status

        timerTask = object : CountDownTimer(time, 1000) {
            override fun onFinish() {
                startTimer(timezone)
            }

            override fun onTick(millis: Long) {
                fireUpdate(millis)
            }
        }.start()
    }

    private fun fireUpdate(millis: Long) {
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(millis)
        val minsLeft = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hoursLeft))
        val secondsLeft = TimeUnit.MILLISECONDS
            .toSeconds(millis - (TimeUnit.HOURS.toMillis(hoursLeft) + TimeUnit.MINUTES.toMillis(minsLeft)))

        view?.updateTimer(hoursLeft, minsLeft, secondsLeft)
    }


}