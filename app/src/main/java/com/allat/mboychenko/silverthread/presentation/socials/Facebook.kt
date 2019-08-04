package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils

class Facebook(val context: Context, val intent: Intent, val path: String) :
    SocialNetworkModel {

    init {
        setData(context, intent)
    }

    override fun open() {
        context.startActivity(intent)
    }

    override fun canOpen(): Boolean = intent.data != null

    override fun setData(context: Context, intent: Intent) {
        val pm = context.packageManager
        try {
            if (pm.getApplicationInfo(FACEBOOK_PACKAGE_NAME, 0).enabled) {
                if (!TextUtils.isEmpty(path)) {
                    intent.data = Uri.parse(FACEBOOK_APP_WEBFACE_BASE_URL + FACEBOOK_BASE_URL + path)
                    return
                }
            }
            intent.data = Uri.parse( FACEBOOK_BASE_URL + path)
        } catch (e: Exception) {
            intent.data = Uri.parse( FACEBOOK_BASE_URL + path)
        }
    }

    companion object {

        const val DEEP_LINK_SCHEMA = "facebook"

        private const val FACEBOOK_PACKAGE_NAME = "com.facebook.katana"
        private const val FACEBOOK_BASE_URL = "https://www.facebook.com"
        private const val FACEBOOK_APP_WEBFACE_BASE_URL = "fb://facewebmodal/f?href="
    }
}
