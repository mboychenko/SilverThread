package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.domain.interactor.ParablesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnComputation
import com.allat.mboychenko.silverthread.presentation.views.fragments.IListFavFragmentView

class ParablesPresenter(
    ctx: Context,
    private val storage: ParablesDetailsStorage
) : ListFavoritePresenter<IListFavFragmentView>(ctx) {

    private val quotesArr: Array<String> by lazy { context.resources.getStringArray(R.array.parables) }

    override fun getFullList() = quotesArr

    override fun getItem(position: Int) = quotesArr[position]

    override fun addToFavorite(position: Int) {
        runTaskOnComputation {
            storage.putFavoriteParablePosition(position)
        }
    }

    override fun removeFromFavorite(position: Int) {
        runTaskOnComputation {
            storage.removeFavoriteParablePosition(position)
        }
    }

    override fun getFavPos(): Set<Int> = storage.getFavoriteParablesPositions()

}