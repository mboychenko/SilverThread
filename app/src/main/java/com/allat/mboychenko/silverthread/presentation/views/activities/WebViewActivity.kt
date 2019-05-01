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

class WebViewActivity : BaseNavigationActivity() {

    private var currentUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUrl = intent?.data.toString()

        webViewWindow.webViewClient = object : WebViewClient() {
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

    override fun setFragment(fragment: Fragment, navId: Int) {
        setResult(RESULT_CODE_SET_FRAGMENT, Intent().apply { putExtra(RESULT_NAV_ID, navId)})
        finish()
    }

    companion object {
        const val REQUEST_CODE_WEB_VIEW = 12// and 0xffff
        const val RESULT_CODE_SET_FRAGMENT = 713
        const val RESULT_NAV_ID = "RESULT_NAV_ID"
    }

}
