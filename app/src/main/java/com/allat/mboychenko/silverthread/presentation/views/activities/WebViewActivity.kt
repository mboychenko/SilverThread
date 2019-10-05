package com.allat.mboychenko.silverthread.presentation.views.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.WEB_DOWNLOADS_FOLDER_NAME
import com.allat.mboychenko.silverthread.presentation.helpers.getExternalStorageAvailableSpaceBytes
import com.allat.mboychenko.silverthread.presentation.helpers.getPublicDownloadsStorageDir
import com.allat.mboychenko.silverthread.presentation.socials.SocialNetworkFactory
import com.allat.mboychenko.silverthread.presentation.helpers.hasInternetAccess
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants.ALLATRA_FILE_SERVER_HOST
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants.URI_ALLATRA_TV
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants.isAllatraResUrl
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.NestedWebView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WebViewActivity : BaseNavigationActivity() {

    private var currentUrl = ""
    private lateinit var progressBar: View
    private lateinit var errorView: View
    private lateinit var webView: NestedWebView
    private lateinit var refresh: TextView
    private lateinit var errorDesc: TextView
    private lateinit var errorTitle: TextView
    private val webChromeClient: MyChrome by lazy { MyChrome() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressBar = findViewById(R.id.progress)
        errorView = findViewById(R.id.error_internet)
        errorTitle = findViewById(R.id.no_internet_title)
        errorDesc = findViewById(R.id.no_internet_desc)
        webView = findViewById(R.id.webViewWindow)
        refresh = findViewById(R.id.refresh)
        refresh.setOnClickListener { refresh() }

        setupWebViewCookies(webView)

        webView.webChromeClient = webChromeClient
        webView.webViewClient = object :
            WebViewClient() {

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return overrideUrlLoading(view, url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString()
                return overrideUrlLoading(view, url, request?.isRedirect == true)
            }

            private fun overrideUrlLoading(
                view: WebView?,
                url: String?,
                redirect: Boolean = false
            ): Boolean {
                if (view == null || url.isNullOrEmpty()) {
                    return false
                }

                val uri = Uri.parse(url)
                if (SocialNetworkFactory.isSocialNetwork(uri)) {
                    val socialNetwork =
                        SocialNetworkFactory.getSocialNetwork(applicationContext, uri)
                    if (socialNetwork != null && socialNetwork.canOpen()) {
                        socialNetwork.open()
                        return true
                    }
                }

                when {
                    url.startsWith(MAILTO) -> {
                        startActivity(Intent(Intent.ACTION_SENDTO, uri))
                        return true
                    }

                    url.startsWith(TEL) -> {
                        startActivity(Intent(Intent.ACTION_DIAL, uri))
                        return true
                    }

                    uri.host == ALLATRA_FILE_SERVER_HOST -> return false

                    redirect && url.contains(HTTP_SCHEME) -> {
                        webView.loadUrl(url.replace(HTTP_SCHEME, HTTPS_SCHEME))
                        return true
                    }

                    url.contains(HTTP_SCHEME) -> {
                        webView.loadUrl(url.replace(HTTP_SCHEME, HTTPS_SCHEME))
                        return true
                    }
                }

                currentUrl = url
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                if (isAllatraResUrl(url)) {
                    updateNavDrawerBy(webViewUrl = url)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.d("onReceivedError", description)
                if (!isAllatraResUrl(failingUrl)) {
                    return
                }

                view?.loadUrl(ABOUT_BLANK)

                progressBar.visibility = View.GONE
                errorView.visibility = View.VISIBLE

                setupError(description ?: "")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("onReceivedError", error?.description.toString())
                    if (!isAllatraResUrl(request?.url.toString())) {
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
                    errorDesc.text = getString(R.string.web_view_net_error_desc, desc)
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

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            if (contentLength <= getExternalStorageAvailableSpaceBytes()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        downloadDialog(url, userAgent, contentDisposition, mimetype)
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1
                        )
                    }
                } else {
                    //Code for devices below API 23 or Marshmallow
                    downloadDialog(url, userAgent, contentDisposition, mimetype)
                }
            } else {
                Toast.makeText(this, R.string.not_enough_memory, Toast.LENGTH_LONG).show()
            }
        }

        currentUrl = savedInstanceState?.getString(SAVED_CURRENT_URL, URI_ALLATRA_TV)
            ?: intent?.data?.toString() ?: URI_ALLATRA_TV

        progressBar.visibility = View.VISIBLE
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(currentUrl)
        }

    }

    private fun setupWebViewCookies(view: WebView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
            CookieManager.getInstance().acceptCookie()
        }
    }

    private fun downloadDialog(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String
    ) {
        val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.download)
        builder.setMessage(getString(R.string.save_web_loading, filename))

        builder.setPositiveButton(R.string.yes) { _, _ ->
            Observable.fromCallable { getPublicDownloadsStorageDir(WEB_DOWNLOADS_FOLDER_NAME) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file ->
                    file?.let {
                        FileLoaderService.commandLoadFile(
                            applicationContext,
                            url,
                            it.path,
                            filename,
                            true
                        )
                    }
                },
                    {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.cant_create_folder_for_downloads, it.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
        }

        builder.setNegativeButton(R.string.cancel)
        { dialog, which ->
            dialog.cancel()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()

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
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun getContentView(): Int {
        return R.layout.activity_web_view
    }

    override fun webViewLink(uri: String) {
        if (webView.url == uri) {
            return
        }

        progressBar.visibility = View.VISIBLE
        currentUrl = uri
        webView.loadUrl(uri)
    }

    override fun onBackPressed() {
        when {
            webChromeClient.mCustomView != null -> webChromeClient.onHideCustomView()
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        outState.putString(SAVED_CURRENT_URL, currentUrl)
        super.onSaveInstanceState(outState)
    }

    override fun setFragment(fragment: Fragment, navId: Int) {
        setResult(RESULT_CODE_SET_FRAGMENT,
            Intent().apply { putExtra(RESULT_NAV_ID, navId) })
        finish()
    }

    private inner class MyChrome : WebChromeClient() {

        internal var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalOrientation: Int = 0
        private var mOriginalSystemUiVisibility: Int = 0

        override fun getDefaultVideoPoster(): Bitmap? =
            if (mCustomView == null) {
                null
            } else {
                BitmapFactory.decodeResource(resources, 2130837573)
            }

        override fun onHideCustomView() {

            val decorView = window.decorView
            if (decorView is FrameLayout) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                decorView.removeView(mCustomView)
            }
            mCustomView = null
            decorView.systemUiVisibility = mOriginalSystemUiVisibility
            requestedOrientation = mOriginalOrientation
            mCustomViewCallback?.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View,
            paramCustomViewCallback: CustomViewCallback
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }

            val decorView = window.decorView
            mCustomView = paramView
            mOriginalSystemUiVisibility = decorView.systemUiVisibility
            mOriginalOrientation = requestedOrientation
            mCustomViewCallback = paramCustomViewCallback
            if (decorView is FrameLayout) {
                getAppBar().setExpanded(false)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                decorView.addView(
                    mCustomView,
                    FrameLayout.LayoutParams(-1, -1)
                )
            }


            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    companion object {
        const val REQUEST_CODE_WEB_VIEW = 12
        const val RESULT_CODE_SET_FRAGMENT = 713
        const val RESULT_NAV_ID = "RESULT_NAV_ID"
        const val SAVED_CURRENT_URL = "SAVED_CURRENT_URL"

        const val ABOUT_BLANK = "about:blank"
        const val MAILTO = "mailto:"
        const val TEL = "tel:"
        const val ERR_INTERNET_DISCONNECTED = "net::ERR_INTERNET_DISCONNECTED"
        const val HTTP_SCHEME = "http://"
        const val HTTPS_SCHEME = "https://"
    }

}
