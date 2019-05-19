package com.allat.mboychenko.silverthread.presentation.views.activities

import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.activities.BaseNavigationActivity
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlin.collections.ArrayList

class WebViewActivity : BaseNavigationActivity() {

    private var currentUrl = ""
    private var webBackstack: MutableList<String> = mutableListOf()

    private fun <E> MutableList<E>.pop(): E? =
        if (this.size - 1 < 0) {
            null
        } else {
            this.removeAt(this.size - 1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUrl = intent?.data.toString()

        webViewWindow.webViewClient = object : WebViewClient() { //todo add loading listener, social network app checking, add link to webBackstack
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
        }
        webViewWindow.settings.cacheMode = WebSettings.LOAD_DEFAULT //LOAD_NO_CACHE check app size with some time
        webViewWindow.settings.javaScriptEnabled = true
        webViewWindow.loadUrl(currentUrl)
    }

    override fun onResume() {
        super.onResume()
        updateNavDrawerBy(webViewUrl = currentUrl)
    }

    override fun getContentView(): Int {
        return R.layout.activity_web_view
    }

    override fun webViewLink(uri: String) {
        if (currentUrl == uri) {
            return
        }

        currentUrl = uri
        webViewWindow.loadUrl(currentUrl)
    }

    override fun onBackPressed() {
        webBackstack.pop()?.let {
            webViewLink(it)
            return
        }

        super.onBackPressed()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVED_CURRENT_URL, currentUrl)
        outState.putStringArrayList(SAVED_WEB_BACKSTACK, webBackstack as ArrayList<String>)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.getString(SAVED_CURRENT_URL)?.let {
            currentUrl = it
        }
        savedInstanceState?.getStringArrayList(SAVED_WEB_BACKSTACK)?.let {
            webBackstack = it.toMutableList()
        }
    }

    override fun setFragment(fragment: Fragment, navId: Int) {
        setResult(RESULT_CODE_SET_FRAGMENT, Intent().apply { putExtra(RESULT_NAV_ID, navId)})
        finish()
    }

    companion object {
        const val REQUEST_CODE_WEB_VIEW = 12
        const val RESULT_CODE_SET_FRAGMENT = 713
        const val RESULT_NAV_ID = "RESULT_NAV_ID"
        const val SAVED_CURRENT_URL = "SAVED_CURRENT_URL"
        const val SAVED_WEB_BACKSTACK = "SAVED_WEB_BACKSTACK"
    }

}
