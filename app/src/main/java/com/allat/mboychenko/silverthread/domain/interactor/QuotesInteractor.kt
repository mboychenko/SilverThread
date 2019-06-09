package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.Storage

class QuotesInteractor(val storage: Storage): QuotesDetailsStorage {

    override fun getFavoriteQuotesPositions(): Set<Int> {
        val set = storage.getStringSet(QUOTES_FAVORITES_PREF_KEY)
        return set.map { it.toInt() }.toSet()
    }

    override fun putFavoriteQuotePosition(quotePos: Int) {
        val set = storage.getStringSet(QUOTES_FAVORITES_PREF_KEY).toMutableSet()
        set.add(quotePos.toString())
        storage.putStringSet(QUOTES_FAVORITES_PREF_KEY, set)
    }

    override fun removeFavoriteQuotePosition(quotePos: Int) {
        val set = storage.getStringSet(QUOTES_FAVORITES_PREF_KEY).toMutableSet()
        set.remove(quotePos.toString())
        storage.putStringSet(QUOTES_FAVORITES_PREF_KEY, set)
    }

    companion object {
        private const val QUOTES_FAVORITES_PREF_KEY = "QUOTES_FAVORITES_PREF_KEY"
    }


}