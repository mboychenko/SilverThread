package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class BaseAllatRaFragment : Fragment(), IAllatRaFragments {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarName(activity)
    }

    override fun getViewContext(): Context? = context

}