package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.allat.mboychenko.silverthread.presentation.views.activities.BaseNavigationActivity

interface IAllatRaFragments {

    fun getFragmentTag(): String

    fun getViewContext(): Context?

    fun shareText(extraText: String, chooserTitle: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "AllatRa Content")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, extraText)
        getViewContext()?.startActivity(Intent.createChooser(sharingIntent, chooserTitle))
    }

    fun copyToClipboard(context: Context?, copy: String) {
        context?.let {
            val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("AllatRa Content", copy)
            clipboard.primaryClip = clip
        }
    }

    fun getToolbar(activity: FragmentActivity?) = activity?.let { (it as BaseNavigationActivity).getToolbar() }

    fun getDrawer(activity: FragmentActivity?) = activity?.let { (it as BaseNavigationActivity).getDrawer() }
}

