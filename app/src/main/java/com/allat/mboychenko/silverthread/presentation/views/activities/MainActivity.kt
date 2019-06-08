package com.allat.mboychenko.silverthread.presentation.views.activities

import android.content.Intent
import android.net.Uri
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.activities.BaseNavigationActivity
import com.allat.mboychenko.silverthread.presentation.views.activities.WebViewActivity.Companion.REQUEST_CODE_WEB_VIEW
import com.allat.mboychenko.silverthread.presentation.views.activities.WebViewActivity.Companion.RESULT_CODE_SET_FRAGMENT
import com.allat.mboychenko.silverthread.presentation.views.activities.WebViewActivity.Companion.RESULT_NAV_ID

class MainActivity : BaseNavigationActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun onResume() {
        super.onResume()
        turnOffToolbarScrolling()
        navigationItemPosUpdate()
    }

    override fun webViewLink(uri: String) {
        startActivityForResult(Intent(this, WebViewActivity::class.java).apply {
            data = Uri.parse(uri)
        }, REQUEST_CODE_WEB_VIEW)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WEB_VIEW) {
            when(resultCode) {
                RESULT_CODE_SET_FRAGMENT -> { data?.extras?.getInt(RESULT_NAV_ID)?.let { setFragmentByNavId(it, true) } }
            }
        }
    }

}
