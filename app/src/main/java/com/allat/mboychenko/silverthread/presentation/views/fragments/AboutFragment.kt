package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.content.Context
import androidx.fragment.app.Fragment

class AboutFragment : Fragment(), IAllatRaFragments {

    override fun getFragmentTag(): String = ABOUT_FRAGMENT_TAG

    override fun getViewContext(): Context? = context

    companion object {
        const val ABOUT_FRAGMENT_TAG = "ABOUT_FRAGMENT_TAG"
    }

}