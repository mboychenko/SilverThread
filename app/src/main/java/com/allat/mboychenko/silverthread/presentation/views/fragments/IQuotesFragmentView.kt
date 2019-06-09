package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem

interface IQuotesFragmentView : IAllatRaFragments {
    fun quotesReady(quotes: List<QuoteItem>)
    fun removeItem(item: QuoteItem)
}