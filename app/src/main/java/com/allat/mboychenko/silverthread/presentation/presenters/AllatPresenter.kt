package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import android.os.CountDownTimer
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.domain.interactor.AllatTimeZoneStorage
import com.allat.mboychenko.silverthread.domain.interactor.AppSettingsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper.TimeStatus.*
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.views.fragments.IAllatFragmentView
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class AllatPresenter(
    private val context: Context,
    private val allatStorage: AllatTimeZoneStorage,
    private val appStorage: AppSettingsStorage
) :
    BasePresenter<IAllatFragmentView>() {

    private val allatStatusAwait: String by lazy { context.getString(R.string.time_to_group_meditation) }
    private val allatStatusOngoing: String by lazy { context.getString(R.string.allat_started_meditation) }

    private var timerStatus: AllatHelper.TimeStatus by Delegates.observable(INIT) { _, old, new ->
        if (old != new) {
            view?.updateTimerStatus(if (new == AWAITING) allatStatusAwait else allatStatusOngoing)
        }
    }

    private var timerTask: CountDownTimer? = null

    override fun attachView(view: IAllatFragmentView) {
        super.attachView(view)
        val timezone = allatStorage.getAllatTimezone()
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

    fun setAllatReminder(mins: Int) {
        runTaskOnComputation {
            allatStorage.putAllatNotificationBefore(mins)
            val timezone = allatStorage.getAllatTimezone()
            setupAllatBeforeAlarm(context, mins, timezone)
        }
    }

    fun removeAllatReminder() {
        runTaskOnComputation {
            allatStorage.removeAllatNotification()
            removeAlarm(context, AlarmNotificationCodes.ALLAT_BEFORE.action, AlarmNotificationCodes.ALLAT_BEFORE.code)
        }
    }

    fun getAllatNotifIn(): Int =
        allatStorage.getAllatNotificationBeforeMins()


    fun setAllatTimeZone(timezone: AllatTimeZone) {
        runTaskOnComputation {
            allatStorage.putAllatTimezone(timezone)
            reInitTimers(
                context, timezone,
                allatStorage.getAllatNotificationBeforeMins(),
                allatStorage.getAllatNotificationStart(),
                allatStorage.getAllatNotificationEnd(),
                true
            )
        }

        runTaskOnBackgroundWithResult(
            ExecutorThread.IO,
            {
                appStorage.appFirstLaunch()
            },
            {
                firstLaunch -> if (firstLaunch) view?.openDrawer()
            }
        )

        view?.let {
            it.changeTimezoneSetupVisibility(false)
            startTimer(timezone)
        }
    }

    private fun startTimer(timezone: AllatTimeZone) {
        timerTask?.cancel()

        val (time, status) = AllatHelper.getAllatTimeStatus(timezone)
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

    fun isAllatNotificationStartEnabled() = allatStorage.getAllatNotificationStart()

    fun isAllatNotificationEndEnabled() = allatStorage.getAllatNotificationEnd()

    fun startStopAlarm(alarmNotificationCodes: AlarmNotificationCodes, enable: Boolean) {
        runTaskOnComputation {
            val timezone = allatStorage.getAllatTimezone()

            when (alarmNotificationCodes) {
                AlarmNotificationCodes.ALLAT_START -> {
                    allatStorage.allatNotificationStart(enable)
                    if (enable) setupAllatStartAlarm(context, timezone)
                }
                AlarmNotificationCodes.ALLAT_END -> {
                    allatStorage.allatNotificationEnd(enable)
                    if (enable) setupAllatEndAlarm(context, timezone)
                }
                else -> Unit
            }

            if (enable.not()) {
                removeAlarm(context, alarmNotificationCodes.action, alarmNotificationCodes.code)
            }
        }
    }

}