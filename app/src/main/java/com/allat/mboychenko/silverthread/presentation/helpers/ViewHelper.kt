package com.allat.mboychenko.silverthread.presentation.helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
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

fun SpannableString.alignCenter(start: Int, end: Int) {
    setSpan(
        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun SpannableString.makeBold(start: Int, end: Int) {
    setSpan(
        StyleSpan(Typeface.BOLD),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun SpannableString.changeSize(size: Int, start: Int, end: Int) {
    setSpan(
        AbsoluteSizeSpan(size, true),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun SpannableString.changeColor(context: Context, color: Int, start: Int, end: Int) {
    setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, color)),
        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}