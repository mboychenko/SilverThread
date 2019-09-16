package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.Storage
import java.util.*

class QuotesInteractor(val storage: Storage): QuotesDetailsStorage {

    override fun getFavoriteQuotesPositions(): Set<Int> {
        val set = storage.getStringSet(QUOTES_FAVORITES_PREF_KEY)
        return set.map { it.toInt() }.toSet()
    }

    override fun putFavoriteQuotePosition(quotePos: Int) {
        val set = storage.getStringSet(QUOTES_FAVORITES_PREF_KEY).toMutableSet()
        val quotePosition = quotePos.toString()
        if (!set.contains(quotePosition)) {
            set.add(quotePosition)
            storage.putStringSet(QUOTES_FAVORITES_PREF_KEY, set)
        }
    }

    override fun removeFavoriteQuotePosition(quotePos: Int) {
        val set = storage.getStringSet(QUOTES_FAVORITES_PREF_KEY).toMutableSet()
        val quotesPosition = quotePos.toString()
        if (set.contains(quotesPosition)) {
            set.remove(quotesPosition)
            storage.putStringSet(QUOTES_FAVORITES_PREF_KEY, set)
        }
    }

    override fun getRandomQuotesTimesInDay(): Int =
        storage.getIntDefault(RANDOM_QUOTES_TIMES_PREF_KEY, 0)

    override fun setRandomQuotesTimesInDay(times: Int) {
        storage.putInt(RANDOM_QUOTES_TIMES_PREF_KEY, times)
    }

    override fun getQuotesWasShowedTimesInDay(): Pair<Int, Long> {
        val day = storage.getLongDefault(RANDOM_QUOTES_SHOWED_DAY_PREF_KEY, Calendar.getInstance().timeInMillis)
        val times = storage.getIntDefault(RANDOM_QUOTES_SHOWED_TIMES_PREF_KEY, 0)
        return Pair(times, day)
    }

    override fun setQuotesWasShowedTimesInDay(timesInDay: Pair<Int, Long>) {
        storage.putInt(RANDOM_QUOTES_SHOWED_TIMES_PREF_KEY, timesInDay.first)
        storage.putLong(RANDOM_QUOTES_SHOWED_DAY_PREF_KEY, timesInDay.second)
    }

    override fun clearShowedTimesInDay() {
        storage.remove(RANDOM_QUOTES_SHOWED_TIMES_PREF_KEY)
        storage.remove(RANDOM_QUOTES_SHOWED_DAY_PREF_KEY)
    }

    companion object {
        private const val QUOTES_FAVORITES_PREF_KEY = "QUOTES_FAVORITES_PREF_KEY"
        private const val RANDOM_QUOTES_TIMES_PREF_KEY = "RANDOM_QUOTES_TIMES_PREF_KEY"
        private const val RANDOM_QUOTES_SHOWED_TIMES_PREF_KEY = "RANDOM_QUOTES_SHOWED_TIMES_PREF_KEY"
        private const val RANDOM_QUOTES_SHOWED_DAY_PREF_KEY = "RANDOM_QUOTES_SHOWED_DAY_PREF_KEY"
    }


}