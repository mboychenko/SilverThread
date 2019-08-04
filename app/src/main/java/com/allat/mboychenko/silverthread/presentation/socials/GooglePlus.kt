package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri

class GooglePlus(val context: Context, val intent: Intent, val path: String) :
    SocialNetworkModel {

    init {
        setData(context, intent)
    }

    override fun open() {
        context.startActivity(intent)
    }

    override fun canOpen(): Boolean = intent.data != null


    override fun setData(context: Context, intent: Intent) {
        intent.data = Uri.parse(GPLUS_BASE_URL + path)
    }

    companion object {
        const val SCHEMA = "plus.google"
        const val GPLUS_BASE_URL = "https://plus.google.com"
    }
}