package com.allat.mboychenko.silverthread.domain.interactor

interface ParablesDetailsStorage {
    fun getFavoriteParablesPositions(): Set<Int>
    fun putFavoriteParablePosition(parablePos: Int)
    fun removeFavoriteParablePosition(parablePos: Int)
    fun restoreFavoriteParables(restoredParables: Set<Int>)
}