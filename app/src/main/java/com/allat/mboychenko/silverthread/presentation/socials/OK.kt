package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri

class OK(val context: Context, val intent: Intent, val path: String) :
    SocialNetworkModel {

    init {
        setData(context, intent)
    }

    override fun open() {
        context.startActivity(intent)
    }

    override fun canOpen(): Boolean = intent.data != null


    override fun setData(context: Context, intent: Intent) {
        intent.data = Uri.parse(OK_BASE_URL + path)
    }

    companion object {
        const val DEEP_LINK_SCHEMA = "odnoklassniki"
        const val DEEP_LINK_SCHEMA_ALT = "ok"

        private const val OK_BASE_URL = "https://ok.ru"
    }


}
