package com.allat.mboychenko.silverthread.presentation.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.content.Context.CONNECTIVITY_SERVICE
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


/** CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT  */

fun isNetworkAvailable(context: Context): Boolean {

    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager
            .getNetworkCapabilities(network)

        capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        activeNetworkInfo != null
    }

}

private fun hasInternetAccess(context: Context): Boolean {
    if (isNetworkAvailable(context)) {
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
    return false
}

@SuppressLint("CheckResult")
fun hasInternetAccess(context: Context, observer: (Boolean) -> Unit): Disposable  {
    return Observable.fromCallable { hasInternetAccess(context) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result -> observer(result) }
}
