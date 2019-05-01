package com.allat.mboychenko.silverthread.presentation.services

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.allat.mboychenko.silverthread.AllatRaApplication
import java.util.*
import java.util.concurrent.TimeUnit

class AllatRadioService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


//                    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);
//                        r.play();
}