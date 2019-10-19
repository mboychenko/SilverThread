package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri

class VK(val context: Context, val intent: Intent, val path: String) :
    SocialNetworkModel {

    init {
        setData(context, intent)
    }

    override fun open() {
        context.startActivity(intent)
    }

    override fun canOpen(): Boolean = intent.data != null


    override fun setData(context: Context, intent: Intent) {
        intent.data = Uri.parse(VK_BASE_URL + path)
    }

    companion object {

        const val HOST = "vk"
        const val HOST_ALT = "vkontakte"

        private const val VK_BASE_URL = "https://vk.com"
    }


}
