package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri

class Youtube(val context: Context, val intent: Intent, val path: String) :
    SocialNetworkModel {

    init {
        setData(context, intent)
    }

    override fun open() {
        context.startActivity(intent)
    }

    override fun canOpen(): Boolean = intent.data != null


    override fun setData(context: Context, intent: Intent) {
        intent.data = Uri.parse(YOUTUBE_BASE_URL + path)
    }

    companion object {

        const val DEEP_LINK_SCHEMA = "youtube"

        private const val YOUTUBE_BASE_URL = "https://www.youtube.com"
    }


}
