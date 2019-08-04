package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeItem

interface IPracticesFragmentView {
    fun onPracticesByCategoryReady(practices: List<PracticeItem>)
    fun hasPractices():Boolean
}