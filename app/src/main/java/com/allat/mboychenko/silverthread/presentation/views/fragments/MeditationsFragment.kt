package com.allat.mboychenko.silverthread.presentation.views.fragments

import androidx.fragment.app.Fragment

class MeditationsFragment: Fragment(), IAllatRaFragments {

    override fun getFragmentTag(): String = MEDITATION_FRAGMENT_TAG
//        todo описание в заметках

    companion object {
        const val MEDITATION_FRAGMENT_TAG = "MEDITATION_FRAGMENT_TAG"
    }
}