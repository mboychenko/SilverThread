package com.allat.mboychenko.silverthread.presentation.views.activities

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.socials.SocialNetworkFactory
import com.allat.mboychenko.silverthread.presentation.helpers.hasInternetAccess
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants.URI_ALLATRA_TV
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants.isAllatraResUrl
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.NestedWebView
import java.util.*
import kotlin.collections.ArrayList

class WebViewActivity : BaseNavigationActivity() {

    private var currentUrl = ""
    private var webBackStackQueue = Collections.asLifoQueue(LinkedList<String>())
    private lateinit var progressBar: View
    private lateinit var errorView: View
    private lateinit var webView: NestedWebView
    private lateinit var refresh: TextView
    private lateinit var errorDesc: TextView
    private lateinit var errorTitle: TextView

    private fun <E> Queue<E>.addUrl(value: E) {
        if (value == ABOUT_BLANK) {
            return
        }
        this.remove(value)
        this.add(value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUrl = savedInstanceState?.getString(SAVED_CURRENT_URL, URI_ALLATRA_TV) ?: intent?.data?.toString() ?: URI_ALLATRA_TV
        savedInstanceState?.getStringArrayList(SAVED_WEB_BACKSTACK)?.let {
            webBackStackQueue.clear()
            webBackStackQueue.addAll(it)
        }


        progressBar = findViewById(R.id.progress)
        errorView = findViewById(R.id.error_internet)
        errorTitle = findViewById(R.id.no_internet_title)
        errorDesc = findViewById(R.id.no_internet_desc)
        webView = findViewById(R.id.webViewWindow)
        refresh = findViewById(R.id.refresh)
        refresh.setOnClickListener { refresh() }

        setupWebViewCookies(webView)

        webView.webViewClient = object :
            WebViewClient() {

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return overrideUrlLoading(view, url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                return overrideUrlLoading(view, url, request?.isRedirect == true)
            }

            private fun overrideUrlLoading(view: WebView?, url: String?, redirect: Boolean = false): Boolean {
                if (view == null || url.isNullOrEmpty()) {
                    return false
                }

                if (redirect && url.contains(HTTP_SCHEME)) {
                    webBackStackQueue.poll()
                    webView.loadUrl(url.replace(HTTP_SCHEME, HTTPS_SCHEME))
                    return true
                } else if (url.contains(HTTP_SCHEME)) {
                    webView.loadUrl(url.replace(HTTP_SCHEME, HTTPS_SCHEME))
                    return true
                }

                val socialUri = Uri.parse(url)
                if (SocialNetworkFactory.isSocialNetwork(socialUri)) {
                    val socialNetwork = SocialNetworkFactory.getSocialNetwork(applicationContext, socialUri)
                    if (socialNetwork != null && socialNetwork.canOpen()) {
                        socialNetwork.open()
                        return true
                    }
                }

                currentUrl = url
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { webBackStackQueue.addUrl(it) }
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                Log.d("onReceivedError", description)
                if (!isAllatraResUrl(failingUrl) && description == ERR_NAME_NOT_RESOLVED) {
                    return
                }

                view?.loadUrl(ABOUT_BLANK)

                progressBar.visibility = View.GONE
                errorView.visibility = View.VISIBLE

                setupError(description ?: "")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("onReceivedError", error?.description.toString())
                    if (!isAllatraResUrl(request?.url.toString()) && error?.description.toString() == ERR_NAME_NOT_RESOLVED) {
                        return
                    }

                    setupError(error?.description?.toString() ?: "")
                }

                view?.loadUrl(ABOUT_BLANK)

                progressBar.visibility = View.GONE
                errorView.visibility = View.VISIBLE
            }

            private fun setupError(desc: String) {
                if (desc == ERR_INTERNET_DISCONNECTED) {
                    errorTitle.text = getString(R.string.web_view_no_internet)
                    errorDesc.text = ""
                } else {
                    errorTitle.text = getString(R.string.web_view_general_error)
                    errorDesc.text =  getString(R.string.web_view_net_error_desc, desc)
                }
            }
        }

        webView.settings.apply {
            cacheMode = WebSettings.LOAD_DEFAULT //LOAD_NO_CACHE check app size with some time
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
        }

        progressBar.visibility = View.VISIBLE
        webView.loadUrl(currentUrl)

    }

    private fun setupWebViewCookies(view: WebView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
            CookieManager.getInstance().acceptCookie()
        }
    }

    private fun refresh() {
        errorView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        hasInternetAccess(applicationContext) { networkAvailable ->
            if (networkAvailable) {
                webView.loadUrl(currentUrl)
                errorView.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                errorView.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        updateNavDrawerBy(webViewUrl = currentUrl)
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun getContentView(): Int {
        return R.layout.activity_web_view
    }

    override fun webViewLink(uri: String) {
        if (currentUrl == uri) {
            return
        }

        progressBar.visibility = View.VISIBLE
        currentUrl = uri
        webView.loadUrl(currentUrl)
    }

    override fun onBackPressed() {
        webBackStackQueue.poll()
        webBackStackQueue.poll()?.let {
            webViewLink(it)
            return
        }

        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVED_CURRENT_URL, currentUrl)
        outState.putStringArrayList(SAVED_WEB_BACKSTACK, webBackStackQueue.toMutableList() as ArrayList<String>)
    }

    override fun setFragment(fragment: Fragment, navId: Int) {
        setResult(RESULT_CODE_SET_FRAGMENT,
            Intent().apply { putExtra(RESULT_NAV_ID, navId) })
        finish()
    }

    companion object {
        const val REQUEST_CODE_WEB_VIEW = 12
        const val RESULT_CODE_SET_FRAGMENT = 713
        const val RESULT_NAV_ID = "RESULT_NAV_ID"
        const val SAVED_CURRENT_URL = "SAVED_CURRENT_URL"
        const val SAVED_WEB_BACKSTACK = "SAVED_WEB_BACKSTACK"

        const val ABOUT_BLANK = "about:blank"
        const val ERR_NAME_NOT_RESOLVED = "net::ERR_NAME_NOT_RESOLVED"
        const val ERR_INTERNET_DISCONNECTED = "net::ERR_INTERNET_DISCONNECTED"
        const val HTTP_SCHEME = "http://"
        const val HTTPS_SCHEME = "https://"
    }

}
