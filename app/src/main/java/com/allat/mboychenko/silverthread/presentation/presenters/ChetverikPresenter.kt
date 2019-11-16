package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.allat.mboychenko.silverthread.domain.interactor.ChetverikStorage
import com.allat.mboychenko.silverthread.presentation.helpers.ExecutorThread
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackground
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackgroundWithResult
import com.allat.mboychenko.silverthread.presentation.models.ChetverikStage
import com.allat.mboychenko.silverthread.presentation.services.ChetverikService
import com.allat.mboychenko.silverthread.presentation.services.ChetverikService.Companion.ACTION_START
import com.allat.mboychenko.silverthread.presentation.views.fragments.IChetverikFragmentView
import java.util.concurrent.TimeUnit

class ChetverikPresenter(val context: Context, val storage: ChetverikStorage) :
    BasePresenter<IChetverikFragmentView>() {

    private var chetverikService: ChetverikService? = null

    private var chetverikStagesCallback: ChetverikService.ChetverikActionsCallback =
        object : ChetverikService.ChetverikActionsCallback {
            override fun onStageChanged(stage: ChetverikStage) {
                view?.stageChanged(stage)
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
                chetverikService = (it as ChetverikService.LocalBinder).getService()
                chetverikService!!.setCallback(chetverikStagesCallback)
                chetverikStagesCallback.onStageChanged(chetverikService!!.getStatus())
                chetverikStagesCallback.timeLeft(chetverikService!!.currentLeftMillis)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            chetverikService = null
        }
    }

    override fun attachView(view: IChetverikFragmentView) {
        super.attachView(view)
        setupOffsetIfNeed()
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

    override fun detachView() {
        super.detachView()
        unbindFromChetverikService()
        chetverikService = null
    }

    private fun bindToChetverikService() {
        context.bindService(Intent(context, ChetverikService::class.java), serviceConnection, 0)
    }

    private fun unbindFromChetverikService() {
        context.unbindService(serviceConnection)
    }

    fun startStop() {
        if (chetverikService != null && chetverikService!!.getStatus() != ChetverikStage.INIT) {
            stop()
        } else {
            view?.let {
                storage.setStartOffsetSeconds(it.getOffset())
            }
            start()
        }
    }

    fun start() {
        startChetverikService()
        bindToChetverikService()
    }

    fun stop() {
        if (chetverikService != null) {
            chetverikService!!.stopChetverik()
        } else {
            context.stopService(Intent(context, ChetverikService::class.java))
            view?.stageChanged(ChetverikStage.INIT)
        }
    }

    private fun startChetverikService() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, ChetverikService::class.java).apply { action = ACTION_START })
    }
}