package com.allat.mboychenko.silverthread.domain.interactor

interface QuotesDetailsStorage {
    fun getFavoriteQuotesPositions(): Set<Int>
    fun putFavoriteQuotePosition(quotePos: Int)
    fun removeFavoriteQuotePosition(quotePos: Int)
    //TODO
    //random notification
    //random notification times setting
}