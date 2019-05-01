package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import android.os.CountDownTimer
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneStorage
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.presentation.views.fragments.IAllatFragmentView
import java.util.concurrent.TimeUnit

class AllatPresenter(private val context: Context, private val storage: AllatTimeZoneStorage) : BasePresenter<IAllatFragmentView>(){

    private var timerStatus: AllatHelper.TimeStatus = AllatHelper.TimeStatus.AWAITING

    private var timerTask: CountDownTimer? = null

    override fun attachView(view: IAllatFragmentView) {
        super.attachView(view)
        startTimer(storage.getAllatTimezone())
    }

    override fun detachView() {
        super.detachView()
        timerTask?.cancel()
    }

    fun setAllatTimeZone(timezone: AllatTimeZone) { //todo
        storage.putAllatTimezone(timezone)
        startTimer(timezone)
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

        view?.updateTimer(hoursLeft, minsLeft, secondsLeft, timerStatus)
    }


}