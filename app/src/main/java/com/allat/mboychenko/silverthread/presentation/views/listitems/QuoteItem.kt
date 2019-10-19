package com.allat.mboychenko.silverthread.presentation.views.listitems

import android.view.Menu
import com.allat.mboychenko.silverthread.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.quote_item_layout.view.*
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat


class QuoteItem(
    private val quoteText: String,
    private val actionListener: QuotesActionListener,
    val arrayPosition: Int,
    var favorite: Boolean
) : Item() {

    override fun getLayout(): Int = R.layout.quote_item_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {
        with(viewHolder.itemView) {

            if (favorite) {
                card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            } else {
                val array = context.theme.obtainStyledAttributes(
                    intArrayOf(android.R.attr.colorBackground)
                )
                card.setCardBackgroundColor(array.getColor(0, 0xFF00FF))
            }


            quote.text = quoteText
            buttonMore.setOnClickListener { onPopupMenuClick(it) }
        }
    }

    private fun onPopupMenuClick(view: View) {
        val popup = PopupMenu(view.context, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.quote_popup_menu, popup.menu)

        popup.menu.add(
            Menu.NONE,
            FAVORITE_MENU_ITEM_ID,
            1,
            view.context.getString(if (favorite) R.string.remove_from_fav else R.string.add_to_fav)
        )

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_quote_copy -> {
                    actionListener.onCopy(quoteText)
                    true
                }
                R.id.menu_quote_share -> {
                    actionListener.onShare(quoteText)
                    true
                }
                FAVORITE_MENU_ITEM_ID -> {
                    onFavoriteClick()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun onFavoriteClick() {
        actionListener.onFavoriteClick(this)
        favorite = favorite.not()
        notifyChanged()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoteItem

        if (arrayPosition != other.arrayPosition) return false

        return true
    }

    override fun hashCode(): Int {
        return arrayPosition.hashCode()
    }


    interface QuotesActionListener {
        fun onShare(quote: String)
        fun onCopy(quote: String)
        fun onFavoriteClick(quoteItem: QuoteItem)
    }

    companion object {
        private const val FAVORITE_MENU_ITEM_ID = 863
    }
}

