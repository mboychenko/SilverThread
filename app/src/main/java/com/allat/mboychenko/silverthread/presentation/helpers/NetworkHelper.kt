package com.allat.mboychenko.silverthread.presentation.helpers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.net.ConnectivityManager
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import android.net.NetworkCapabilities
import android.os.Build
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import android.net.Network
import android.net.NetworkRequest
import android.net.ConnectivityManager.NetworkCallback
import android.os.Handler
import android.os.Looper

private fun hasInternetAccess(context: Context): Boolean {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager
            .getNetworkCapabilities(network)

        if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return true
        }

    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            try {
                val urlc = URL("https://clients3.google.com/generate_204")
                    .openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Android")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1500
                urlc.connect()
                return urlc.responseCode == 204 && urlc.contentLength == 0
            } catch (e: IOException) {
                Log.e("NetworkHelper", "Error checking internet connection", e)
            }

        } else {
            Log.d("NetworkHelper", "No network available!")
        }
    }

    return false
}

@SuppressLint("CheckResult")
fun hasInternetAccess(context: Context, observer: (Boolean) -> Unit): Disposable {
    return Observable.fromCallable { hasInternetAccess(context) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { result -> observer(result) }
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ConnectionStateMonitor(
    val context: Context,
    private val callerThread: Handler = Handler(Looper.getMainLooper()),
    val callback: InternetAvailability
) : NetworkCallback() {

    @Volatile private var hasInternet = false

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkRequest: NetworkRequest =
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()

    fun enable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(networkRequest, this)
        } else {
            context.registerReceiver(internetStateReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        }
    }

    fun disable() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(this)
            } else {
                context.unregisterReceiver(internetStateReceiver)
            }
        } catch (e: IllegalArgumentException) {
            //if disable called before enable
        }
    }

    /**
     * Calling on background ConnectivityThread
     */
    override fun onAvailable(network: Network) {
        if (!hasInternet) {
            Handler().postDelayed({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        hasInternet = true
                        executeOnHandlerThread(callerThread) { callback.internetAvailable() }
                    }
                } else {
                    val activeNetworkInfo = connectivityManager.activeNetworkInfo
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected && hasInternetAccess(context)) {
                        hasInternet = true
                        executeOnHandlerThread(callerThread) { callback.internetAvailable() }
                    }
                }
            }, 1500)        //delay for NET_CAPABILITY_VALIDATED, its need some time depend on network, usually 1.5 sec enough.
        }
    }

    /**
     * Calling on background ConnectivityThread
     */
    override fun onLost(network: Network?) {
        hasInternet = false
        executeOnHandlerThread(callerThread) { callback.internetUnavailable() }
    }

    /**
     * Calling on background ConnectivityThread
     */
    override fun onUnavailable() {
        hasInternet = false
        executeOnHandlerThread(callerThread) { callback.internetUnavailable() }
    }

    private val internetStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                hasInternetAccess(it) { available ->
                    if (available) {
                        callback.internetAvailable()
                    } else {
                        callback.internetUnavailable()
                    }
                }
            }
        }
    }
}

interface InternetAvailability {
    fun internetAvailable()
    fun internetUnavailable()
}