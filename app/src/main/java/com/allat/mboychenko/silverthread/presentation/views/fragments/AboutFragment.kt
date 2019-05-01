package com.allat.mboychenko.silverthread.presentation.views.fragments

import androidx.fragment.app.Fragment

class AboutFragment : Fragment(), IAllatRaFragments {

    override fun getFragmentTag(): String = ABOUT_FRAGMENT_TAG

    companion object {
        const val ABOUT_FRAGMENT_TAG = "ABOUT_FRAGMENT_TAG"
    }

}