package com.allat.mboychenko.silverthread.presentation.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.allat.mboychenko.silverthread.presentation.helpers.*
import android.os.*
import com.allat.mboychenko.silverthread.domain.interactor.ChetverikStorage
import com.allat.mboychenko.silverthread.presentation.models.ChetverikStage
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.allat.mboychenko.silverthread.R



class ChetverikService : Service() {

    private val chetverikStorage: ChetverikStorage by inject()

    private lateinit var  mediaPlayer: MediaPlayer
    private lateinit var mAudioManager: AudioManager
    private var originalVolume: Int = 0

    private val iBinder = LocalBinder()
    private var chetverikStagesViewCallback: ChetverikActionsCallback? = null
    private var stagesQueue = ArrayDeque<ChetverikStage>(
        mutableListOf(
            ChetverikStage.START,
            ChetverikStage.RIGHT,
            ChetverikStage.LEFT,
            ChetverikStage.BACK,
            ChetverikStage.FRONT
        )
    )

    private var stage by Delegates.observable(ChetverikStage.INIT) { _: KProperty<*>, _: ChetverikStage, new: ChetverikStage ->
            updateNotification(
                applicationContext,
                NOTIFICATION_ID_CHETVERIK,
                getChetverikNotification(
                    applicationContext,
                    new
                )
            )
            chetverikStagesViewCallback?.onStageChanged(new)
    }

    var currentLeftMillis: Long = 0

    override fun onCreate() {
        super.onCreate()
          mAudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
          originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
          mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
                start()
            }
            setOnCompletionListener {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                reset()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    private lateinit var startIntervalTimer: CountDownTimer
    private val allatIntervalTimer: CountDownTimer = object : CountDownTimer(ONE_ALLAT_MILLIS, 1000) {
        override fun onFinish() {
            try {
                stage = stagesQueue.pop()
                chetverikStagesViewCallback?.timeLeft(ONE_ALLAT_MILLIS)
                currentLeftMillis = ONE_ALLAT_MILLIS
                playSound(R.raw.chetverik_stage)
            } catch (ex: NoSuchElementException) {
                updateNotification(
                    applicationContext,
                    NOTIFICATION_ID_CHETVERIK,
                    getChetverikNotification(
                        applicationContext,
                        ChetverikStage.END
                    )
                )
                playSound(R.raw.chetverik_end)
                stopChetverik()
                return
            }
            start()
        }

        override fun onTick(millis: Long) {
            currentLeftMillis = millis
            chetverikStagesViewCallback?.timeLeft(millis)
        }
    }

    fun playSound(@RawRes rawResId: Int) {
        val assetFileDescriptor = applicationContext.resources.openRawResourceFd(rawResId) ?: return
        mediaPlayer.run {
            reset()
            setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
            prepareAsync()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action == ACTION_START) {
                stage = stagesQueue.pop()
                val startOffset = chetverikStorage.getStartOffsetSeconds()
                if (startOffset > 0) {
                    val delayInMillis = startOffset.toLong() * 1000
                    startIntervalTimer = object : CountDownTimer(delayInMillis, 1000) {
                        override fun onFinish() {
                            stage = stagesQueue.pop()
                            chetverikStagesViewCallback?.timeLeft(ONE_ALLAT_MILLIS)
                            currentLeftMillis = ONE_ALLAT_MILLIS
                            playSound(R.raw.chetverik_stage)
                            allatIntervalTimer.start()
                        }

                        override fun onTick(millis: Long) {
                            currentLeftMillis = millis
                            chetverikStagesViewCallback?.timeLeft(millis)
                        }

                    }
                    chetverikStagesViewCallback?.timeLeft(delayInMillis)
                    startIntervalTimer.start()
                } else {
                    stage = stagesQueue.pop()
                    allatIntervalTimer.start()
                }
                startForeground(NOTIFICATION_ID_CHETVERIK, getChetverikNotification(applicationContext, stage))
            } else if (intent.action == ACTION_STOP) {
                stopChetverik()
            }
        } else {
            stopChetverik()
        }
        return START_STICKY
    }

    fun stopChetverik() {
        if(::startIntervalTimer.isInitialized) {
            startIntervalTimer.cancel()
        }
        allatIntervalTimer.cancel()
        chetverikStagesViewCallback?.onStageChanged(ChetverikStage.INIT)
        stopForeground(true)
        stopSelf()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        chetverikStagesViewCallback = null
        return super.onUnbind(intent)
    }

    fun getStatus(): ChetverikStage {
        return stage
    }

    fun setCallback(chetverikStagesCallback: ChetverikActionsCallback) {
        chetverikStagesViewCallback = chetverikStagesCallback
    }

    inner class LocalBinder : Binder() {
        fun getService(): ChetverikService {
            return this@ChetverikService
        }
    }

    interface ChetverikActionsCallback {
        fun onStageChanged(stage: ChetverikStage)
        fun timeLeft(millis: Long)
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ONE_ALLAT_MILLIS = 717000L //00:11:56.74 rounded to 00:11:57 Allat
    }

}