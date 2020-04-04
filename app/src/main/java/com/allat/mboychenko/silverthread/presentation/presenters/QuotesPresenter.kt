package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.ExecutorThread
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackgroundWithResult
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnComputation
import com.allat.mboychenko.silverthread.presentation.views.fragments.IListFavFragmentView
import java.util.*

class QuotesPresenter(
    ctx: Context,
    private val storage: QuotesDetailsStorage
) : ListFavoritePresenter<IListFavFragmentView>(ctx) {

    private val quotesArr: Array<String> by lazy { context.resources.getStringArray(R.array.quotes) }

    override fun getFullList(): Array<String> = quotesArr

    override fun getItem(position: Int) = quotesArr[position]

    fun getRandomQuote(): Pair<Int, String> {
        val pos = Random().nextInt(quotesArr.size)
        return Pair(pos, quotesArr[pos])
    }

    override fun getFavPos() = storage.getFavoriteQuotesPositions()

    fun isQuoteInFav(position: Int, resultConsumer: (result: Boolean) -> Unit) {
        manageAddToSubscription(
            runTaskOnBackgroundWithResult(
                ExecutorThread.IO,
                {
                    val favSet = storage.getFavoriteQuotesPositions()
                    favSet.contains(position)
                },
                resultConsumer
            )
        )
    }

    override fun addToFavorite(position: Int) {
        runTaskOnComputation {
            storage.putFavoriteQuotePosition(position)
        }
    }

    override fun removeFromFavorite(position: Int) {
        runTaskOnComputation {
            storage.removeFavoriteQuotePosition(position)
        }
    }

}