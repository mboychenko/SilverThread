package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri

class Youtube(val context: Context, val intent: Intent, val host: String, val path: String) :
    SocialNetworkModel {

    init {
        setData(context, intent)
    }

    override fun open() {
        context.startActivity(intent)
    }

    override fun canOpen(): Boolean = intent.data != null


    override fun setData(context: Context, intent: Intent) {
        if (host == HOST) {
            intent.data = Uri.parse(YOUTUBE_BASE_URL + path)
        } else if (host == HOST_ALT) {
            intent.data = Uri.parse(YOUTUBE_SHORT_URL + path)
        }
    }

    companion object {

        const val HOST = "youtube"
        const val HOST_ALT = "youtu"

        private const val YOUTUBE_BASE_URL = "https://www.youtube.com"
        private const val YOUTUBE_SHORT_URL = "https://youtu.be"
    }


}
