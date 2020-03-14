package com.allat.mboychenko.silverthread.domain.interactor

interface QuotesDetailsStorage {
    fun getFavoriteQuotesPositions(): Set<Int>
    fun putFavoriteQuotePosition(quotePos: Int)
    fun restoreFavoriteQuotes(restoredQuotes: Set<Int>)
    fun removeFavoriteQuotePosition(quotePos: Int)

    fun getRandomQuotesTimesInDay(): Int
    fun setRandomQuotesTimesInDay(times: Int)

    fun getQuotesWasShowedTimesInDay(): Pair<Int, Long>
    fun setQuotesWasShowedTimesInDay(timesInDay: Pair<Int, Long>)

    fun saveNextQuoteTime(nextTime: Long)
    fun getNextQuoteTime(): Long
    fun removeNextQuoteTime()

    fun clearShowedTimesInDay()
}