package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils

class TG(val context: Context, val intent: Intent, val path: String) :
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

        if (path.startsWith(DEEP_LINK_SCHEMA)) {
            try {
                if (pm.getApplicationInfo(TG_PACKAGE_NAME, 0).enabled) {
                    if (!TextUtils.isEmpty(path)) {
                        intent.data = Uri.parse(path)
                        return
                    }
                }
                intent.data = Uri.parse( TG_BASE_URL + path.substringAfter("="))
            } catch (e: Exception) {
                intent.data = Uri.parse( TG_BASE_URL + path.substringAfter("="))
            }
        } else {
            intent.data = Uri.parse(TG_BASE_URL + path)
        }
    }

    companion object {

        const val TG_PACKAGE_NAME = "org.telegram.messenger"
        const val DEEP_LINK_SCHEMA = "tg:resolve"
        const val SCHEMA = "t"
        const val SCHEMA_ALT = "tg"

        private const val TG_BASE_URL = "https://t.me"
    }


}