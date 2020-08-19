package com.allat.mboychenko.silverthread.presentation.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.allat.mboychenko.silverthread.presentation.helpers.*
import android.os.*
import com.allat.mboychenko.silverthread.presentation.models.PracticeStage
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty
import android.media.MediaPlayer
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.RawRes
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryNoteEditorDialog


class PracticeService : Service() {

    private lateinit var  mediaPlayer: MediaPlayer
    private lateinit var mAudioManager: AudioManager
    private var originalVolume: Int = 0
    private var allatSize: Long = ONE_ALLAT_MILLIS
    private var isHigh : Boolean = true
    private val iBinder = LocalBinder()
    private var stagesViewCallback: PracticeActionsCallback? = null

    private var allatArray = ArrayDeque<Int>()

    private var currentAllat: Int = 1

    private var stage: PracticeStage by Delegates.observable(PracticeStage.INIT) { _: KProperty<*>, _: PracticeStage, new: PracticeStage ->
        updateNotification(
            applicationContext,
            NOTIFICATION_ID_CHETVERIK,
            getPracticeNotification(
                applicationContext,
                new,
                currentAllat
            )
        )
        stagesViewCallback?.onStageChanged(new, currentAllat)
    }

    var currentLeftMillis: Long = 0

    //for f*cking Xiaomi, Meizu, etc
    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PracticeService:WakeLock"
            )
        }
    }

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
    private val allatIntervalTimer: CountDownTimer by lazy {
        object : CountDownTimer(allatSize, 1000) {
            override fun onFinish() {
                try {
                    currentAllat = allatArray.pop()
                    stage = PracticeStage.ALLAT
                    stagesViewCallback?.timeLeft(allatSize)
                    currentLeftMillis = allatSize
                    if(isHigh) {
                        playSound(R.raw.practice_end_higher)
                    } else {
                        playSound(R.raw.practice_stage)
                    }
                } catch (ex: NoSuchElementException) {
                    if(isHigh) {
                        playSound(R.raw.practice_end_higher)
                    } else {
                        playSound(R.raw.practice_end)
                    }
                    stopTimer()
                    return
                }
                start()
            }

            override fun onTick(millis: Long) {
                currentLeftMillis = millis
                stagesViewCallback?.timeLeft(millis)
            }
        }
    }

    fun playSound(@RawRes rawResId: Int) {
        val assetFileDescriptor =
            applicationContext.resources.openRawResourceFd(rawResId) ?: return

        mediaPlayer.run {
            reset()
            setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
            prepareAsync()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action == ACTION_START) {
                stage = PracticeStage.START
                val startOffset = intent.extras?.getInt(EXTRAS_OFFSET_KEY,0) ?: 0
                val allats = intent.extras?.getInt(EXTRAS_ALLATS_NUM_KEY,1) ?: 1
                val allatLengthFull = intent.extras?.getBoolean(EXTRAS_ALLATS_LEN_FULL_KEY,true) ?: true
                isHigh = intent.extras?.getBoolean(EXTRAS_VOLUME_HIGH_FULL_KEY,true) ?: true

                if (!allatLengthFull) {
                    allatSize = HALF_ALLAT_MILLIS
                }

                for (n in 1..allats) {
                    allatArray.addLast(n)
                }

                val delayInMillis = startOffset.toLong() * 1000
                acquireWakeLock(allats * allatSize + delayInMillis)

                if (delayInMillis > 0) {
                    startIntervalTimer = object : CountDownTimer(delayInMillis, 1000) {
                        override fun onFinish() {
                            currentAllat = allatArray.pop()
                            stage = PracticeStage.ALLAT
                            stagesViewCallback?.timeLeft(allatSize)
                            currentLeftMillis = allatSize
                            if(isHigh) {
                                playSound(R.raw.practice_end_higher)
                            }else {
                                playSound(R.raw.practice_stage)
                            }
                            allatIntervalTimer.start()
                        }

                        override fun onTick(millis: Long) {
                            currentLeftMillis = millis
                            stagesViewCallback?.timeLeft(millis)
                        }

                    }
                    stagesViewCallback?.timeLeft(delayInMillis)
                    startIntervalTimer.start()
                } else {
                    currentAllat = allatArray.pop()
                    stage = PracticeStage.ALLAT
                    if(isHigh) {
                        playSound(R.raw.practice_end_higher)
                    } else {
                        playSound(R.raw.practice_end)
                    }
                    allatIntervalTimer.start()
                }
                startForeground(NOTIFICATION_ID_CHETVERIK, getPracticeNotification(applicationContext, stage, currentAllat))
            } else if (intent.action == ACTION_STOP) {
                stopTimer()
            }
        } else {
            stopTimer()
        }
        return START_STICKY
    }

    private fun acquireWakeLock(millis: Long) {
        wakeLock.acquire(millis + 5000)
    }


    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    fun stopTimer() {
        if(::startIntervalTimer.isInitialized) {
            startIntervalTimer.cancel()
        }
        allatIntervalTimer.cancel()
        stagesViewCallback?.onStageChanged(PracticeStage.INIT)
        stopForeground(true)
        releaseWakeLock()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stagesViewCallback = null
        return super.onUnbind(intent)
    }

    fun getStatus(): PracticeStage {
        return stage
    }

    fun getCurAllat(): Int {
        return currentAllat
    }

    fun setCallback(practiceStagesCallback: PracticeActionsCallback) {
        stagesViewCallback = practiceStagesCallback
    }

    inner class LocalBinder : Binder() {
        fun getService(): PracticeService {
            return this@PracticeService
        }
    }

    interface PracticeActionsCallback {
        fun onStageChanged(stage: PracticeStage, curAllat: Int = 1)
        fun timeLeft(millis: Long)
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRAS_OFFSET_KEY = "EXTRAS_OFFSET_KEY"
        const val EXTRAS_ALLATS_NUM_KEY = "EXTRAS_ALLATS_NUM_KEY"
        const val EXTRAS_ALLATS_LEN_FULL_KEY = "EXTRAS_ALLATS_LEN_FULL_KEY"
        const val ONE_ALLAT_MILLIS = 717000L //00:11:56.74 rounded to 00:11:57 Allat
        const val HALF_ALLAT_MILLIS = 358000L //Half Allat
        const val EXTRAS_VOLUME_HIGH_FULL_KEY = "EXTRAS_VOLUME_HIGH_FULL_KEY"
    }

}