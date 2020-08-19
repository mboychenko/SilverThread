package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.allat.mboychenko.silverthread.domain.interactor.PracticeStorage
import com.allat.mboychenko.silverthread.presentation.helpers.ExecutorThread
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackground
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackgroundWithResult
import com.allat.mboychenko.silverthread.presentation.models.PracticeStage
import com.allat.mboychenko.silverthread.presentation.services.PracticeService
import com.allat.mboychenko.silverthread.presentation.services.PracticeService.Companion.ACTION_START
import com.allat.mboychenko.silverthread.presentation.services.PracticeService.Companion.EXTRAS_ALLATS_LEN_FULL_KEY
import com.allat.mboychenko.silverthread.presentation.services.PracticeService.Companion.EXTRAS_VOLUME_HIGH_FULL_KEY
import com.allat.mboychenko.silverthread.presentation.services.PracticeService.Companion.EXTRAS_ALLATS_NUM_KEY
import com.allat.mboychenko.silverthread.presentation.services.PracticeService.Companion.EXTRAS_OFFSET_KEY
import com.allat.mboychenko.silverthread.presentation.views.fragments.IPracticeTimerFragmentView
import java.util.concurrent.TimeUnit

class PracticeTimerPresenter(private val context: Context, private val storage: PracticeStorage) :
    BasePresenter<IPracticeTimerFragmentView>() {

    private var practiceService: PracticeService? = null

    private var practiceStagesCallback: PracticeService.PracticeActionsCallback =
        object : PracticeService.PracticeActionsCallback {
            override fun onStageChanged(stage: PracticeStage, curAllat: Int) {
                view?.stageChanged(stage, curAllat)
            }

            override fun timeLeft(millis: Long) {
                val minsLeft = TimeUnit.MILLISECONDS.toMinutes(millis)
                val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.MINUTES.toMillis(minsLeft))
                view?.stageTimeRemaining(minsLeft.toInt(), secondsLeft.toInt())
            }
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service?.let {
                practiceService = (it as PracticeService.LocalBinder).getService()
                practiceService!!.setCallback(practiceStagesCallback)
                practiceStagesCallback.onStageChanged(practiceService!!.getStatus(), practiceService!!.getCurAllat())
                practiceStagesCallback.timeLeft(practiceService!!.currentLeftMillis)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            practiceService = null
        }
    }

    override fun attachView(view: IPracticeTimerFragmentView) {
        super.attachView(view)
        setupOffsetIfNeed()
        setupAllatLengthIfNeed()
        setupVolumeHighSwitch()
        bindToChetverikService()
    }

    private fun setupOffsetIfNeed() {
        manageAddToSubscription(
            runTaskOnBackgroundWithResult(ExecutorThread.IO, {
                storage.getStartOffsetSeconds()
            }, { startOffset ->
                var mins = 0
                val sec: Int
                if (startOffset > 60) {
                    mins = startOffset / 60
                    sec = startOffset % 60
                } else {
                    sec = startOffset
                }
                view?.setMinsViewOffset(mins)
                view?.setSecondsViewOffset(sec)
            })
        )
    }

    private fun setupAllatLengthIfNeed() {
        manageAddToSubscription(
            runTaskOnBackgroundWithResult(ExecutorThread.IO, {
                storage.getAllatLengthStateShort()
            }, { short ->
                view?.setAllatLengthShort(short)
            })
        )
    }

    private fun setupVolumeHighSwitch() {
        manageAddToSubscription(
            runTaskOnBackgroundWithResult(ExecutorThread.IO, {
                storage.getVolumeStateHigh()
            }, { checked ->
                view?.setVolumeHigh(checked)
            })
        )
    }

    override fun detachView() {
        super.detachView()
        unbindFromChetverikService()
        practiceService = null
    }

    private fun bindToChetverikService() {
        context.bindService(Intent(context, PracticeService::class.java), serviceConnection, 0)
    }

    private fun unbindFromChetverikService() {
        context.unbindService(serviceConnection)
    }

    fun setAllatLengthShort(short: Boolean) {
        manageAddToSubscription(runTaskOnBackground { storage.setAllatLengthStateShort(short) })
    }

    fun setVolumeHigh(high: Boolean) {
        manageAddToSubscription(runTaskOnBackground { storage.setVolumeStateHigh(high) })
    }

    fun startStop() {
        if (practiceService != null && practiceService!!.getStatus() != PracticeStage.INIT) {
            stop()
        } else {
            view?.let {
                val offset = it.getOffset()
                manageAddToSubscription(runTaskOnBackground { storage.setStartOffsetSeconds(offset) })
            }
            start()
        }
    }

    fun start() {
        startPracticeService()
        bindToChetverikService()
    }

    fun stop() {
        if (practiceService != null) {
            practiceService!!.stopTimer()
        } else {
            context.stopService(Intent(context, PracticeService::class.java))
            view?.stageChanged(PracticeStage.INIT)
        }
    }

    private fun startPracticeService() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, PracticeService::class.java).apply {
                action = ACTION_START
                putExtras(Bundle().apply {
                    putInt(EXTRAS_OFFSET_KEY, view?.getOffset() ?: 0)
                    putInt(EXTRAS_ALLATS_NUM_KEY, view?.getAllatsNum() ?: 1)

                    val isFullAllat = view?.getAllatLengthShort()?.not() ?: true
                    putBoolean(EXTRAS_ALLATS_LEN_FULL_KEY, isFullAllat)

                    val volumeHigh = view?.getVolumeHigh() ?: true
                    putBoolean(EXTRAS_VOLUME_HIGH_FULL_KEY, volumeHigh)
                })
            })
    }
}