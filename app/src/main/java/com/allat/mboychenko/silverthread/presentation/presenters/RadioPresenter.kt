package com.allat.mboychenko.silverthread.presentation.presenters

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.services.AllatRadioService
import com.allat.mboychenko.silverthread.presentation.views.fragments.IRadioFragmentView
import com.allat.mboychenko.silverthread.presentation.views.fragments.RadioFragment

class RadioPresenter(val context: Context) : BasePresenter<IRadioFragmentView>() {

    var noInternet = true
    private var radioService: AllatRadioService? = null
    private var radioActionsCallback: AllatRadioService.RadioActionsCallback? = null
    private val connectionStateMonitor = ConnectionStateMonitor(context, Handler(), object : InternetAvailability {
        override fun internetAvailable() {
            updateOnlineStatus(true)
        }

        override fun internetUnavailable() {
            updateOnlineStatus(false)
        }
    })

    fun onCreate() {
        runTaskOnBackgroundWithResult(
            ExecutorThread.IO,
            {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            },
            { result ->
                if (result != PackageManager.PERMISSION_GRANTED) {
                    view?.requestPhonePermission()
                }
            })
    }

    private fun updateOnlineStatus(online: Boolean) {
        noInternet = online.not()
        view?.updateOnlineStatus(online)
    }

    override fun attachView(view: IRadioFragmentView) {
        super.attachView(view)
        bindToRadioService()
        checkOnline()
    }

    override fun detachView() {
        super.detachView()
        connectionStateMonitor.disable()
        unbindRadioService()
    }

    fun checkOnline() {
        manageAddToSubscription(
            hasInternetAccess(context) { hasInternet ->
                updateOnlineStatus(hasInternet)
                view?.hideProgress()
            }
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectionStateMonitor.disable()
            service?.let {
                radioService = (it as AllatRadioService.LocalBinder).getService()
                radioActionsCallback?.let { callback ->
                    radioService!!.setCallback(callback)
                    callback.onPlayerStatusChanged(radioService!!.getStatus())      //real status
                }

            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionStateMonitor.enable()
            radioService = null
        }
    }

    private fun connectToRadioService(serviceAction: String) {
        startRadioServiceWithBundle(serviceAction)
        bindToRadioService()
    }

    private fun startRadioServiceWithBundle(act: String) {
        ContextCompat.startForegroundService(context,
            Intent(context, AllatRadioService::class.java)
                .apply { action = act })
    }

    private fun bindToRadioService() {
        createRadioActionsCallback()
        context.bindService(Intent(context, AllatRadioService::class.java), serviceConnection, 0)
    }

    private fun unbindRadioService() {
        context.unbindService(serviceConnection)
        connectionStateMonitor.enable()
        radioService = null
    }

    private fun createRadioActionsCallback() {
        radioActionsCallback = object : AllatRadioService.RadioActionsCallback {
            override fun onInternetAvailable() {
                updateOnlineStatus(true)
            }

            override fun onInternetGone() {
                updateOnlineStatus(false)
            }

            override fun onPlayerStatusChanged(status: AllatRadioService.PlaybackStatus) {
                when (status) {
                    AllatRadioService.PlaybackStatus.STOPPED,
                    AllatRadioService.PlaybackStatus.INIT -> {
                        if (view?.getCurrentPlayerButtonsState() != RadioFragment.PlayerButtonsState.INIT) {
                            view?.stopButtonState()
                        }
                        view?.updateOnAirStatus(R.color.red)
                    }
                    AllatRadioService.PlaybackStatus.PAUSED -> {
                        if (view?.getCurrentPlayerButtonsState() != RadioFragment.PlayerButtonsState.PAUSED) {
                            view?.pauseButtonState()
                        }
                        view?.updateOnAirStatus(R.color.yellow, R.string.on_air_paused)
                    }
                    AllatRadioService.PlaybackStatus.PLAYING -> {
                        if (view?.getCurrentPlayerButtonsState() != RadioFragment.PlayerButtonsState.PLAYING) {
                            view?.playButtonState()
                        }
                        view?.updateOnAirStatus(R.color.green)
                    }
                    AllatRadioService.PlaybackStatus.IDLE -> {
                        when {
                            noInternet -> {
                                view?.playButtonsIdleState()
                                view?.updateOnAirStatus(R.color.red)
                            }
                            view?.getCurrentPlayerButtonsState() != RadioFragment.PlayerButtonsState.INIT -> {
                                view?.stopButtonState()
                                view?.updateOnAirStatus(R.color.yellow, R.string.radio_error)
                            }
                            else -> {
                                view?.updateOnAirStatus(R.color.red)
                            }
                        }
                    }
                    AllatRadioService.PlaybackStatus.BUFFERING -> {
                        view?.updateOnAirStatus(R.color.yellow, R.string.buffering)
                    }
                }
            }
        }
    }

    fun play() {
        if (radioService != null) {
            radioService?.resume()
        } else {
            connectToRadioService(AllatRadioService.ACTION_PLAY)
        }
    }

    fun pause() {
        radioService?.pause()
    }

    fun stop() {
        radioService?.stop()
    }

}