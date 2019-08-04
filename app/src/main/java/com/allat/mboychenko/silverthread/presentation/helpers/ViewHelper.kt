package com.allat.mboychenko.silverthread.presentation.helpers

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun <T : View> Fragment.bind(@IdRes res: Int) =
    lazy { this.view!!.findViewById<T>(res) } //todo refactoring for fragments; replace from access from onCreateView to onViewCreated

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun fromHtmlCompat(context: Context?, @StringRes resId: Int): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(context?.getString(resId) ?: "", Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(context?.getString(resId) ?: "")
    }
}