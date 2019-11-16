package com.allat.mboychenko.silverthread.presentation.views.fragments

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.allat.mboychenko.silverthread.presentation.views.activities.BaseNavigationActivity

interface IAllatRaFragments: IViewContext {

    fun getFragmentTag(): String

    fun getToolbar(activity: FragmentActivity?) = activity?.let { (it as BaseNavigationActivity).getToolbar() }

    fun setToolbarName(activity: FragmentActivity?) = getToolbar(activity)?.setTitle(toolbarTitle())

    @StringRes
    fun toolbarTitle(): Int

    fun getDrawer(activity: FragmentActivity?) = activity?.let { (it as BaseNavigationActivity).getDrawer() }
}

