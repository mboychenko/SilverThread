package com.allat.mboychenko.silverthread.presentation.views.fragments.webview

object AllatRaWebViewURIConstants {

    const val URI_ALLATRA_TV_IM = "https://allatra.tv/programs-with-im"
    const val URI_ALLATRA_TV = "https://allatra.tv"
    const val URI_ZNAI = "https://znai.allatra.tv"
    const val URI_ALLATRA_ORG = "https://allatra.org"
    const val URI_VESTI = "https://allatravesti.com"
    const val URI_SCIENCE = "https://allatra-science.org"
    const val URI_PARTNER = "https://allatra-partner.org"
    const val URI_CRAUD = "https://allatra.in/"
    const val URI_GEO = "https://geocenter.info/"

    const val ALLATRA_FILE_SERVER_HOST = "files.allatra.tv"

    fun isAllatraResUrl(url: String?): Boolean {
        if (url == null) {
            return false
        }

        return url.startsWith(URI_ALLATRA_TV) ||
                url.startsWith(URI_ALLATRA_TV_IM) ||
                url.startsWith(URI_ZNAI) ||
                url.startsWith(URI_ALLATRA_ORG) ||
                url.startsWith(URI_VESTI) ||
                url.startsWith(URI_SCIENCE) ||
                url.startsWith(URI_PARTNER) ||
                url.startsWith(URI_CRAUD) ||
                url.startsWith(URI_GEO)
    }

}