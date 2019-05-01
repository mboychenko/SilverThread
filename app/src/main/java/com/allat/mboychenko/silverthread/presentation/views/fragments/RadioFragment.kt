package com.allat.mboychenko.silverthread.presentation.views.fragments

import androidx.fragment.app.Fragment

class RadioFragment: Fragment(), IAllatRaFragments, IRadioFragmentView {

    override fun getFragmentTag(): String = RADIO_FRAGMENT_TAG

    companion object {
        const val RADIO_FRAGMENT_TAG = "RADIO_FRAGMENT_TAG"
    }

}