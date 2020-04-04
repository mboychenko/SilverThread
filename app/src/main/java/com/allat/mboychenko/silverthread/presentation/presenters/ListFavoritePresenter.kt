package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.ExecutorThread
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackgroundWithResult
import com.allat.mboychenko.silverthread.presentation.views.fragments.IListFavFragmentView
import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem

abstract class ListFavoritePresenter<T : IListFavFragmentView>(protected val context: Context) :
    BasePresenter<T>() {

    protected var listState: ListState = ListState.NORMAL

    fun getListFavState() = listState

    fun changeListFavState(state: ListState? = null) {
        listState = state ?: if (listState == ListState.NORMAL) ListState.FAVORITE else ListState.NORMAL
        view?.let { loadItemsByState() }
    }

    private val visibleItems = mutableListOf<QuoteItem>()

    fun loadItemsByState() {
        manageAddToSubscription(
            runTaskOnBackgroundWithResult(
                ExecutorThread.COMPUTATION,
                {
                    val quotesItems = mutableListOf<QuoteItem>()
                    val favorites = getFavPos()
                    val parableStyle = this is ParablesPresenter

                    if (listState == ListState.FAVORITE) {
                        favorites.forEach {
                            if (it in getFullList().indices) {
                                quotesItems.add(QuoteItem(getFullList()[it], moreActionListener, it, true, parableStyle))
                            }
                        }
                    } else {
                        quotesItems.addAll(getFullList().mapIndexed { index, s ->
                            QuoteItem(s, moreActionListener, index, favorites.contains(index), parableStyle)
                        })
                    }
                    quotesItems
                },
                {
                    visibleItems.clear()
                    visibleItems.addAll(it)
                    view?.updateList(visibleItems)
                })
        )
    }

    fun filterVisibleQuotes(search: String?): List<QuoteItem> {
        return if (search.isNullOrEmpty()) {
            visibleItems
        } else {
            visibleItems.filter { quote -> quote.quoteText.contains(search, true) }
        }
    }

    protected abstract fun getFullList(): Array<String>

    abstract fun getFavPos(): Set<Int>

    abstract fun getItem(position: Int): String

    abstract fun addToFavorite(position: Int)

    abstract fun removeFromFavorite(position: Int)

    private val moreActionListener = object : QuoteItem.QuotesActionListener {
        override fun onShare(item: String) {
            view?.shareText(item, context.getString(R.string.share_quote))
        }

        override fun onCopy(item: String) {
            view?.copyToClipboard(context, item)
        }

        override fun onFavoriteClick(item: QuoteItem) {
            if (item.favorite) {
                removeFromFavorite(item.arrayPosition)
                if (listState == ListState.FAVORITE) {
                    view?.removeFavItem(item)
                }
            } else {
                addToFavorite(item.arrayPosition)
            }
        }
    }

    enum class ListState {
        NORMAL,
        FAVORITE
    }
}