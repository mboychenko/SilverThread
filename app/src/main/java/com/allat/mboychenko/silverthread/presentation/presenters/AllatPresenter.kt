package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import com.allat.mboychenko.silverthread.domain.interactor.AllatNotificationsSettingsStorage
import com.allat.mboychenko.silverthread.domain.interactor.AppSettingsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper
import com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper.TimeStatus.*
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.views.fragments.IAllatFragmentView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class AllatPresenter(
    private val context: Context,
    private val allatStorage: AllatNotificationsSettingsStorage,
    private val appStorage: AppSettingsStorage
) :
    BasePresenter<IAllatFragmentView>() {

    private val allatStatusAwait: String by lazy { context.getString(R.string.time_to_group_meditation) }
    private val allatStatusOngoing: String by lazy { context.getString(R.string.allat_started_meditation) }
    private val notificationMinutesBeforeArray : IntArray by lazy { context.resources.getIntArray(R.array.notify_before_mins) }
    private val simpleDateFormat by lazy {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
            timeZone = AllatTimeZone.GMT.getCalendarTimezone()
        }
    }

    private var timerStatus: AllatHelper.TimeStatus by Delegates.observable(INIT) { _, old, new ->
        if (old != new) {
            view?.updateTimerStatus(if (new == AWAITING) allatStatusAwait else allatStatusOngoing)
        }
    }

    private var allatTimerTask: CountDownTimer? = null
    private var verdictTimerTask: CountDownTimer? = null


    override fun attachView(view: IAllatFragmentView) {
        super.attachView(view)
        manageAddToSubscription(
            runTaskOnBackgroundWithResult(
                ExecutorThread.IO,
                {
                    allatStorage.getAllatTimezone()
                },
                {
                    if (it != AllatTimeZone.NOT_INIT) {
                        view.changeTimezoneSetupVisibility(false)
                        startAllatTimer(it)
                    } else {
                        view.changeTimezoneSetupVisibility(true)
                    }
                }
            )
        )
        startVerdictTimer()
        initViews()
    }

    override fun detachView() {
        super.detachView()
        allatTimerTask?.cancel()
        verdictTimerTask?.cancel()
    }

    private fun startAllatTimer(timezone: AllatTimeZone) {
        allatTimerTask?.cancel()

        val (time, status) = AllatHelper.getAllatTimeStatus(timezone)
        timerStatus = status

        allatTimerTask = object : CountDownTimer(time, 1000) {
            override fun onFinish() {
                startAllatTimer(timezone)
            }

            override fun onTick(millis: Long) {
                fireAllatUpdate(millis)
            }
        }.start()
    }

    private fun startVerdictTimer() {
        verdictTimerTask?.cancel()
        val (verdictStage, verdictDate) = getNextStageTime()

        view?.updateVerdictTitle(context.getString(verdictStage.resId))
        view?.updateVerdictSubtitle(simpleDateFormat.format(verdictDate.time))


        val currTime = Calendar.getInstance()
        var timeLeft = verdictDate.timeInMillis - currTime.timeInMillis
        fireVerdictUpdate(timeLeft)
        val secOffset = currTime.get(Calendar.SECOND)

        Handler().postDelayed({
            timeLeft = verdictDate.timeInMillis - Calendar.getInstance().timeInMillis
            fireVerdictUpdate(timeLeft)
            verdictTimerTask = object : CountDownTimer(timeLeft, 60_000) {
                override fun onFinish() {}

                override fun onTick(millis: Long) {
                    fireVerdictUpdate(millis)
                }
            }.start()
        }, (60 - secOffset) * 1000L)
    }

    private fun fireAllatUpdate(millis: Long) {
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(millis)
        val minsLeft = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hoursLeft))
        val secondsLeft = TimeUnit.MILLISECONDS
            .toSeconds(millis - (TimeUnit.HOURS.toMillis(hoursLeft) + TimeUnit.MINUTES.toMillis(minsLeft)))

        view?.updateAllatTimer(hoursLeft, minsLeft, secondsLeft)
    }

    private fun fireVerdictUpdate(millis: Long) {
        val daysLeft = TimeUnit.MILLISECONDS.toDays(millis)
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(millis - TimeUnit.DAYS.toMillis(daysLeft))
        val minsLeft =
            TimeUnit.MILLISECONDS.toMinutes(millis - (TimeUnit.DAYS.toMillis(daysLeft) + TimeUnit.HOURS.toMillis(hoursLeft)))

        view?.updateVerdictTimer(daysLeft.toInt(), hoursLeft.toInt(), minsLeft.toInt())
    }

    private fun initViews() {
        manageAddToSubscription(
            runTaskOnBackground(ExecutorThread.IO) {
                val mins = allatStorage.getAllatNotificationBeforeMins()
                val startEnabled = allatStorage.getAllatNotificationStart()
                val endEnabled = allatStorage.getAllatNotificationEnd()
                val loud = allatStorage.allatSoundLoud()

                executeOnMainThread {
                    view?.apply {
                        allatNotifIn(mins)
                        ringOnStartEnabled(startEnabled)
                        ringOnEndEnabled(endEnabled)
                        setAllatSoundLoud(loud)
                    }
                }
            }
        )
    }

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


    fun setRingLevel(loud: Boolean) {
        runTaskOnBackground {
            allatStorage.setAllatSoundLoud(loud)
        }
    }

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
            startAllatTimer(timezone)
        }
    }


    private fun setAllatReminder(mins: Int) {
        runTaskOnComputation {
            allatStorage.putAllatNotificationBefore(mins)
            val timezone = allatStorage.getAllatTimezone()
            setupAllatBeforeAlarm(context, mins, timezone)
        }
    }

    private fun removeAllatReminder() {
        runTaskOnComputation {
            allatStorage.removeAllatNotification()
            removeAlarm(
                context,
                AlarmNotificationCodes.ALLAT_BEFORE.action,
                AlarmNotificationCodes.ALLAT_BEFORE.code
            )
        }
    }

    fun allatNotifInSelected(position: Int) {
        runTaskOnComputation {
            val allatNotifInMinutes = allatStorage.getAllatNotificationBeforeMins()
            if (position == 0) {    //None
                if (allatNotifInMinutes != -1) {
                    removeAllatReminder()
                }
            } else {
                if (allatNotifInMinutes != notificationMinutesBeforeArray[position - 1]) {
                    setAllatReminder(notificationMinutesBeforeArray[position - 1])
                }
            }
        }
    }
}