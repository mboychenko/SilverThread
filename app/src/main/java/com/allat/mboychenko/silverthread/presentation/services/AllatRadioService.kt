package com.allat.mboychenko.silverthread.presentation.services

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import android.media.AudioManager
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.storage.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import org.koin.android.ext.android.inject
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.media.session.MediaButtonReceiver
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.views.activities.MainActivity

class AllatRadioService : Service(), Player.EventListener, AudioManager.OnAudioFocusChangeListener,
    InternetAvailability {

    private val exoPlayerCache: SimpleCache by inject()

    private val iBinder = LocalBinder()
    private var status = PlaybackStatus.INIT

    private lateinit var player: SimpleExoPlayer
    private lateinit var transportControls: MediaControllerCompat.TransportControls

    private val allatRadioStreaminUri: Uri by lazy { Uri.parse(applicationContext.getString(R.string.allatra_radio_url)) }
    private val classicMusicStreamingUri: Uri by lazy { Uri.parse(applicationContext.getString(R.string.classic_radio_url)) }
    private lateinit var currentStreamUri: Uri

    private var onGoingCall = false
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var mediaSession: MediaSessionCompat
    private var focusRequest: AudioFocusRequest? = null

    private lateinit var handler: Handler
    private var audioFocusAcquired: Boolean = false
    private var connectionStateMonitor: ConnectionStateMonitor? = null

    private var radioActionsCallback: RadioActionsCallback? = null

    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    )

    override fun onPlayerError(error: ExoPlaybackException?) {
        //cover it?
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.volume = 0.8f
                resume()
            }

            AudioManager.AUDIOFOCUS_LOSS -> transportControls.pause()

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying()) pause()

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (isPlaying()) {
                player.volume = 0.1f
            }
        }
    }

    lateinit var audioManager: AudioManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            startForeground(NOTIFICATION_ID_RADIO, getRadioNotification(applicationContext, status, mediaSession))
            when (intent.action) {
                ACTION_RESUME -> resume()
                ACTION_PLAY -> transportControls.play()
                ACTION_PAUSE -> transportControls.pause()
                ACTION_STOP -> transportControls.stop()
                else -> MediaButtonReceiver.handleIntent(mediaSession, intent)
            }
        } else if (status == PlaybackStatus.INIT) {
            transportControls.play()
        }

        return START_REDELIVER_INTENT
    }

    private fun requestAudioFocus(): Boolean {
        if (!audioFocusAcquired) {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes =
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()

                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, handler)
                    .build()

                audioManager.requestAudioFocus(focusRequest!!)
            } else {
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                transportControls.stop()
            } else {
                audioFocusAcquired = true
            }
        }

        return audioFocusAcquired
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        currentStreamUri = allatRadioStreaminUri
        handler = Handler()
        initRadio()
    }

    private fun initRadio() {
        status = PlaybackStatus.INIT
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, javaClass.simpleName)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        transportControls = mediaSession.controller.transportControls
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getString(R.string.mediadata_artist))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getString(R.string.mediadata_album))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getString(R.string.mediadata_title_on_air))
                .build()
        )
        mediaSession.setCallback(mediasSessionCallback)
        mediaSession.setSessionActivity(
            getActivityPendingIntent(
                applicationContext,
                NOTIFICATION_ACTION_RADIO,
                flag = PendingIntent.FLAG_UPDATE_CURRENT,
                javaClass = MainActivity::class.java
            )
        )

        val mediaButtonIntent =
            Intent(Intent.ACTION_MEDIA_BUTTON, null, applicationContext, MediaButtonReceiver::class.java)
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0)
        )

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(applicationContext, trackSelector)
        player.addListener(this)

        connectionStateMonitor = ConnectionStateMonitor(applicationContext, handler, this)
        connectionStateMonitor!!.enable()
        registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private var noInternet: Boolean = false

    override fun internetAvailable() {
        radioActionsCallback?.onInternetAvailable()
        noInternet = false
        if (status != PlaybackStatus.INIT) {
            resume()
        }
    }

    override fun internetUnavailable() {
        radioActionsCallback?.onInternetGone()
        noInternet = true
    }

    private var playerInIdle = true
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        status = when (playbackState) {
            Player.STATE_ENDED -> PlaybackStatus.STOPPED
            Player.STATE_IDLE -> PlaybackStatus.IDLE
            Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
            Player.STATE_READY -> if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
            else -> PlaybackStatus.IDLE
        }

        if (status != PlaybackStatus.IDLE) {
            refreshNotificationAndForegroundStatus()
            playerInIdle = false
        } else if (noInternet && !playerInIdle) {   //when internet gone & buffer end
            playerInIdle = true
            status = PlaybackStatus.PAUSED
            refreshNotificationAndForegroundStatus()
            pause()
        } else {
            refreshNotificationAndForegroundStatus()
        }


        radioActionsCallback?.onPlayerStatusChanged(status)
    }

    private fun refreshNotificationAndForegroundStatus() {
        val metaBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getString(R.string.mediadata_artist))
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getString(R.string.mediadata_album))

        when (status) {
            PlaybackStatus.BUFFERING -> {
                mediaSession.setMetadata(
                    metaBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getString(R.string.buffering))
                        .build()
                )
                showNotification(
                    applicationContext,
                    NOTIFICATION_ID_RADIO,
                    getRadioNotification(applicationContext, status, mediaSession)
                )
            }
            PlaybackStatus.PLAYING -> {
                mediaSession.setMetadata(
                    metaBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getString(R.string.mediadata_title_on_air))
                        .build()
                )
                startForeground(NOTIFICATION_ID_RADIO, getRadioNotification(applicationContext, status, mediaSession))
            }
            PlaybackStatus.PAUSED -> {
                val title = if (noInternet && playerInIdle) {
                    getString(R.string.mediadata_title_no_connection)
                } else {
                    getString(R.string.mediadata_title_on_air_paused)
                }

                mediaSession.setMetadata(
                    metaBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .build()
                )
                showNotification(
                    applicationContext,
                    NOTIFICATION_ID_RADIO,
                    getRadioNotification(applicationContext, status, mediaSession)
                )
                stopForeground(false)
            }
            else -> {
                stopForeground(true)
            }
        }
    }

    private fun showNoInternetWarning() {
        Toast.makeText(applicationContext, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
    }

    fun play(streamUrl: Uri = currentStreamUri) {
        if (noInternet) {
            showNoInternetWarning()
        }

        currentStreamUri = streamUrl

        if (requestAudioFocus()) {
            mediaSession.isActive = true
            val cacheDataSourceFactory = CacheDataSourceFactory(applicationContext, exoPlayerCache)
            val audioSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(currentStreamUri)

            player.prepare(audioSource)
            player.playWhenReady = true
            mediaSession.setPlaybackState(
                stateBuilder.setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f
                ).build()
            )
        }

    }

    fun resume() {
        if (status == PlaybackStatus.PLAYING) {
            return
        }

        if ((status == PlaybackStatus.PAUSED || status == PlaybackStatus.BUFFERING) &&
            mediaSession.isActive && requestAudioFocus()
        ) {

            if (noInternet) {
                showNoInternetWarning()
            }

            player.playWhenReady = true
            mediaSession.setPlaybackState(
                stateBuilder.setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f
                ).build()
            )

        } else {
            if (status == PlaybackStatus.STOPPED) {
                initRadio()
            }
            play()
        }
    }

    fun pause() {
        player.playWhenReady = false
        mediaSession.setPlaybackState(
            stateBuilder.setState(
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f
            ).build()
        )
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(this)
        }
        audioFocusAcquired = false
    }

    fun stop() {
        releaseDependencies()
        stopSelf()
    }

    private fun releaseDependencies() {
        try {
            unregisterReceiver(becomingNoisyReceiver)
            connectionStateMonitor?.disable()

            abandonAudioFocus()

            mediaSession.setPlaybackState(
                stateBuilder.setState(
                    PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f
                ).build()
            )
            mediaSession.isActive = false
            mediaSession.release()

            player.stop()
            player.release()
            player.removeListener(this)

            status = PlaybackStatus.STOPPED
            radioActionsCallback?.onPlayerStatusChanged(status)



            if (::telephonyManager.isInitialized)
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)


        } catch (e: IllegalArgumentException) {
            //if already was unregistered with stop and try again onDestroy
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (status == PlaybackStatus.IDLE ||
            status == PlaybackStatus.STOPPED
        ) {
            stop()
        }
        removeCallback()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        releaseDependencies()
        hideNotification(applicationContext,
            Bundle().apply { putInt(NOTIFICATION_CANCEL_ID_EXTRA, NOTIFICATION_ID_RADIO) })
        Log.d("onPlayerStatusChanged", "Destroyed")
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AllatRadioService {
            return this@AllatRadioService
        }
    }

    fun isPlaying(): Boolean {
        return status == PlaybackStatus.PLAYING
    }

    fun getStatus() = status

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            pause()
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                if (!isPlaying()) {
                    return
                }

                onGoingCall = true
                abandonAudioFocus()
                pause()

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (!onGoingCall) {
                    return
                }

                onGoingCall = false
                resume()
            }
        }
    }

    private val mediasSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPause() {
            super.onPause()
            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onPlay() {
            super.onPlay()
            if (status == PlaybackStatus.PAUSED) {
                resume()
            } else {
                play()
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            if (Intent.ACTION_MEDIA_BUTTON == mediaButtonEvent?.action) {
                val keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent
                if (keyEvent.action == KeyEvent.ACTION_DOWN && isSupportedMediaKey(keyEvent.keyCode)) {
                    when (keyEvent.keyCode) {
                        KeyEvent.KEYCODE_HEADSETHOOK,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            if (status == PlaybackStatus.PLAYING) {
                                transportControls.pause()
                            } else if (status == PlaybackStatus.INIT || status == PlaybackStatus.IDLE || status == PlaybackStatus.PAUSED) {
                                transportControls.play()
                            }
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT,
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            if (status == PlaybackStatus.INIT) {
                                transportControls.play()
                            }
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> transportControls.play()
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> transportControls.pause()
                        KeyEvent.KEYCODE_MEDIA_STOP -> transportControls.stop()
                    }
                } else if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                    Log.d("AllatRadioService", "Unsupported media button!")
                    if (status == PlaybackStatus.INIT) {
                        transportControls.play()
                    }
                }
            } else {
                Log.d("AllatRadioService", "Unknown intent: " + mediaButtonEvent?.action)
            }
            return true
        }
    }

    fun isSupportedMediaKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_STOP,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_HEADSETHOOK -> return true
        }
        return false
    }

    fun setCallback(callback: RadioActionsCallback) {
        radioActionsCallback = callback
    }

    private fun removeCallback() {
        radioActionsCallback = null
    }

    interface RadioActionsCallback {
        fun onInternetGone()
        fun onInternetAvailable()
        fun onPlayerStatusChanged(status: PlaybackStatus)
    }

    enum class PlaybackStatus {
        INIT,
        STOPPED,
        IDLE,
        BUFFERING,
        PLAYING,
        PAUSED
    }

    companion object {
        const val ACTION_PLAY = "com.allat.mboychenko.silverthread.notifications.radio.play"
        const val ACTION_PAUSE = "com.allat.mboychenko.silverthread.notifications.radio.pause"
        const val ACTION_RESUME = "com.allat.mboychenko.silverthread.notifications.radio.resume"
        const val ACTION_STOP = "com.allat.mboychenko.silverthread.notifications.radio.stop"
    }

}