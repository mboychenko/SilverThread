package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.preferences.Storage

class ParablesInteractor(private val storage: Storage): ParablesDetailsStorage {

    override fun getFavoriteParablesPositions(): Set<Int> {
        val set = storage.getStringSet(PARABLES_FAVORITES_PREF_KEY)
        return set.map { it.toInt() }.toSet()
    }

    override fun putFavoriteParablePosition(parablePos: Int) {
        val set = storage.getStringSet(PARABLES_FAVORITES_PREF_KEY).toMutableSet()
        val quotePosition = parablePos.toString()
        if (!set.contains(quotePosition)) {
            set.add(quotePosition)
            storage.putStringSet(PARABLES_FAVORITES_PREF_KEY, set)
        }
    }

    override fun removeFavoriteParablePosition(parablePos: Int) {
        val set = storage.getStringSet(PARABLES_FAVORITES_PREF_KEY).toMutableSet()
        val quotesPosition = parablePos.toString()
        if (set.contains(quotesPosition)) {
            set.remove(quotesPosition)
            storage.putStringSet(PARABLES_FAVORITES_PREF_KEY, set)
        }
    }

    override fun restoreFavoriteParables(restoredParables: Set<Int>) {
        val set = storage.getStringSet(PARABLES_FAVORITES_PREF_KEY).toMutableSet()
        restoredParables.forEach {
            val id = it.toString()
            if (!set.contains(id)) {
                set.add(id)
            }
        }
        storage.putStringSet(PARABLES_FAVORITES_PREF_KEY, set)
    }

    companion object {
        private const val PARABLES_FAVORITES_PREF_KEY = "PARABLES_FAVORITES_PREF_KEY"
    }

}