package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.AllatHelper

interface IAllatFragmentView {
    fun updateTimer(h: Long, m: Long, s: Long, status: AllatHelper.TimeStatus)
}