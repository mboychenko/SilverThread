package com.allat.mboychenko.silverthread.presentation.socials

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils

object SocialNetworkFactory {

    private val externalIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            return intent
        }

    private fun getHostWithoutSchemeAndDomain(uri: Uri): String {
        return uri.host?.replaceFirst(URL_PREFIX_REGEX.toRegex(), "")?.substringBeforeLast(".") ?: ""
    }

    fun getSocialNetwork(context: Context, uri: Uri): SocialNetwork? {
        val host = getHostWithoutSchemeAndDomain(uri)
        var path = uri.path ?: ""

        uri.query?.let {
            if (path.isNotEmpty()) {
                path = String.format("%s?%s", path, it)
            }
        }

        if (uri.scheme == TG.DEEP_LINK_SCHEMA) {
            return TG(context,
                externalIntent, uri.toString())
        }

        return when (host) {
            Facebook.HOST -> Facebook(context,
                externalIntent, path)
            Instagram.HOST -> Instagram(context,
                externalIntent, path)
            Twitter.HOST -> Twitter(context,
                externalIntent, path)
            Youtube.HOST_ALT,
            Youtube.HOST -> Youtube(context,
                externalIntent, host, path)
            VK.HOST_ALT,
            VK.HOST -> VK(context,
                externalIntent, path)
            OK.HOST,
            OK.HOST_ALT -> OK(context,
                externalIntent, path)
            TG.HOST_ALT,
            TG.HOST -> TG(context,
                externalIntent, path)
            GooglePlus.HOST -> GooglePlus(context,
                externalIntent, path)
            else -> null
        }
    }

    fun isSocialNetwork(uri: Uri): Boolean {
        if (TextUtils.isEmpty(uri.host)) {
            return false
        }

        val host= getHostWithoutSchemeAndDomain(uri)

        return Facebook.HOST == host ||
                Instagram.HOST == host ||
                Youtube.HOST == host ||
                Youtube.HOST_ALT == host ||
                Twitter.HOST == host ||
                VK.HOST == host ||
                VK.HOST_ALT == host ||
                OK.HOST == host ||
                OK.HOST_ALT == host ||
                TG.HOST == host ||
                TG.HOST_ALT == host ||
                GooglePlus.HOST == host ||
                uri.scheme == TG.DEEP_LINK_SCHEMA
    }

    private const val URL_PREFIX_REGEX = "^(http://www\\.|https://www\\.|http://|https://|www.)"

}

interface SocialNetwork {
    fun open()
    fun canOpen(): Boolean
}

interface SocialNetworkModel : SocialNetwork {
    fun setData(context: Context, intent: Intent)
}