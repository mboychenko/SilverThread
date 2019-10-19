package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.ExecutorThread
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackgroundWithResult
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnComputation
import com.allat.mboychenko.silverthread.presentation.views.fragments.IQuotesFragmentView
import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem
import java.util.*

class QuotesPresenter(
    private val context: Context,
    private val storage: QuotesDetailsStorage
) : BasePresenter<IQuotesFragmentView>() {

    private var quotesState: QuotesState = QuotesState.NORMAL
    private val quotes: Array<String> by lazy { context.resources.getStringArray(R.array.quotes) }

    fun getQuotesState() = quotesState

    fun changeQuotesState(state: QuotesState? = null) {
        quotesState = state ?: if (quotesState == QuotesState.NORMAL) QuotesState.FAVORITE else QuotesState.NORMAL
        view?.let { getQuotes() }
    }

    fun getQuote(position: Int) = quotes[position]

    fun getRandomQuote(): Pair<Int, String> {
        val pos = Random().nextInt(quotes.size)
        return Pair(pos, quotes[pos])
    }

    fun getQuotes() {
        subscriptions.add(
            runTaskOnBackgroundWithResult(
                ExecutorThread.COMPUTATION,
                {
                    val quotesItems = mutableListOf<QuoteItem>()
                    val favorites = storage.getFavoriteQuotesPositions()

                    if (quotesState == QuotesState.FAVORITE) {
                        favorites.forEach {
                            if (it in quotes.indices) {
                                quotesItems.add(QuoteItem(quotes[it], quotesActionListener, it, true))
                            }
                        }
                    } else {
                        quotesItems.addAll(quotes.mapIndexed { index, s ->
                            QuoteItem(s, quotesActionListener, index, favorites.contains(index))
                        })
                    }
                    quotesItems
                },
                { view?.quotesReady(it) })
        )
    }

    fun addToFavorite(position: Int) {
        runTaskOnComputation {
            storage.putFavoriteQuotePosition(position)
        }
    }

    fun removeFromFavorite(position: Int) {
        runTaskOnComputation {
            storage.removeFavoriteQuotePosition(position)
        }
    }

    private val quotesActionListener = object : QuoteItem.QuotesActionListener {
        override fun onShare(quote: String) {
            view?.shareText(quote, context.getString(R.string.share_quote))
        }

        override fun onCopy(quote: String) {
            view?.copyToClipboard(context, quote)
        }

        override fun onFavoriteClick(quoteItem: QuoteItem) {
            if (quoteItem.favorite) {
                removeFromFavorite(quoteItem.arrayPosition)
                if (quotesState == QuotesState.FAVORITE) {
                    view?.removeItem(quoteItem)
                }
            } else {
                addToFavorite(quoteItem.arrayPosition)
            }
        }
    }

    enum class QuotesState {
        NORMAL,
        FAVORITE
    }
}