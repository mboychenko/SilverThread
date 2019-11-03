package com.allat.mboychenko.silverthread.presentation.helpers

import android.content.res.Resources
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.view.View
import androidx.annotation.IdRes
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.fragment.app.Fragment

fun <T : View> Fragment.bind(@IdRes res: Int) =
    lazy { this.view!!.findViewById<T>(res) }

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun htmlFromAssetsCompat(text: String?): Spannable {
    return text?.let { revertSpanned(HtmlCompat.fromHtml(text, FROM_HTML_MODE_COMPACT)) } ?: SpannableString("")
}

private fun revertSpanned(stext: Spanned): Spannable {
    val spans = stext.getSpans(0, stext.length, Any::class.java)
    val ret = Spannable.Factory.getInstance().newSpannable(stext.toString())
    if (spans != null && spans.isNotEmpty()) {
        for (i in spans.indices.reversed()) {
            ret.setSpan(
                spans[i],
                stext.getSpanStart(spans[i]),
                stext.getSpanEnd(spans[i]),
                stext.getSpanFlags(spans[i])
            )
        }
    }
    return ret
}

fun SpannableString.alignRight(start: Int, end: Int) {
    setSpan(
        AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}