package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem

interface IListFavFragmentView : IAllatRaFragments {
    fun updateList(items: List<QuoteItem>)
    fun removeFavItem(item: QuoteItem)
}