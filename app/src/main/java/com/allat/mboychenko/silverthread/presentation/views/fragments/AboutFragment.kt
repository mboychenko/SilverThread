package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.R

class AboutFragment : BaseAllatRaFragment() {

    override fun getFragmentTag(): String = ABOUT_FRAGMENT_TAG

    override fun toolbarTitle(): Int = R.string.about_us

    companion object {
        const val ABOUT_FRAGMENT_TAG = "ABOUT_FRAGMENT_TAG"
    }

}