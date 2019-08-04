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
        getHostWithoutSchemeAndDomain(uri)
        val host =
            getHostWithoutSchemeAndDomain(
                uri
            )
        val path = uri.path ?: ""

        if (uri.toString().startsWith(TG.DEEP_LINK_SCHEMA)) {
            return TG(context,
                externalIntent, uri.toString())
        }

        return when (host) {
            Facebook.DEEP_LINK_SCHEMA -> Facebook(context,
                externalIntent, path)
            Instagram.DEEP_LINK_SCHEMA -> Instagram(context,
                externalIntent, path)
            Twitter.DEEP_LINK_SCHEMA -> Twitter(context,
                externalIntent, path)
            Youtube.DEEP_LINK_SCHEMA -> Youtube(context,
                externalIntent, path)
            VK.DEEP_LINK_SCHEMA_ALT,
            VK.DEEP_LINK_SCHEMA -> VK(context,
                externalIntent, path)
            OK.DEEP_LINK_SCHEMA,
            OK.DEEP_LINK_SCHEMA_ALT -> OK(context,
                externalIntent, path)
            TG.SCHEMA_ALT,
            TG.SCHEMA -> TG(context,
                externalIntent, path)
            GooglePlus.SCHEMA -> GooglePlus(context,
                externalIntent, path)
            else -> null
        }
    }

    fun isSocialNetwork(uri: Uri): Boolean {
        if (TextUtils.isEmpty(uri.host)) {
            return false
        }

        val host =
            getHostWithoutSchemeAndDomain(
                uri
            )

        return Facebook.DEEP_LINK_SCHEMA == host ||
                Instagram.DEEP_LINK_SCHEMA == host ||
                Youtube.DEEP_LINK_SCHEMA == host ||
                Twitter.DEEP_LINK_SCHEMA == host ||
                VK.DEEP_LINK_SCHEMA == host ||
                VK.DEEP_LINK_SCHEMA_ALT == host ||
                OK.DEEP_LINK_SCHEMA == host ||
                OK.DEEP_LINK_SCHEMA_ALT == host ||
                TG.SCHEMA == host ||
                TG.SCHEMA_ALT == host ||
                GooglePlus.SCHEMA == host ||
                uri.toString().startsWith(TG.DEEP_LINK_SCHEMA)
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