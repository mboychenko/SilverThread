package com.allat.mboychenko.silverthread.presentation.views.listitems

import android.view.Menu
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.cardview.widget.CardView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.*


class QuoteItem(
    val quoteText: String,
    private val actionListener: QuotesActionListener,
    val arrayPosition: Int,
    var favorite: Boolean,
    private val parableStyle: Boolean = false
) : Item() {

    private var menuPopupHelper: MenuPopupHelper? = null

    override fun getLayout(): Int = R.layout.quote_item_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {
        with(viewHolder.itemView) {

            findViewById<CardView>(R.id.card).apply {
                if (favorite) {
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                } else {
                    val array = context.theme.obtainStyledAttributes(
                        intArrayOf(android.R.attr.colorBackground)
                    )
                    setCardBackgroundColor(array.getColor(0, 0xFF00FF))
                }
            }

            val quoteTextView = findViewById<TextView>(R.id.quote)

            val buttonMore = findViewById<ImageView>(R.id.buttonMore)
            initPopup(buttonMore)

            val styledResultText = SpannableString(quoteText)

            if (parableStyle) {
                quoteText.indexOf("\n")
                    .takeIf { it > -1 }
                    ?.let {
                        styledResultText.apply {
                            alignCenter(0, it)
                            makeBold(0, it)
                            changeSize(16, 0, it)
                            changeColor(context, R.color.dark_gray, 0, it)
                        }
                    }
                setOnClickListener {
                    quoteTextView.maxLines = if (quoteTextView.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 15
                }
                quoteTextView.maxLines = 15
                quoteTextView.ellipsize = TextUtils.TruncateAt.END
                buttonMore.setOnClickListener { menuPopupHelper?.show() }
            } else {
                quoteText.indexOf("\n").takeIf { it > -1 }
                    ?.let { styledResultText.alignRight(it, quoteText.length) }
                setOnClickListener { menuPopupHelper?.show() }
            }

            quoteTextView.text = styledResultText
        }
    }


    private fun initPopup(view: View) {
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

        menuPopupHelper = MenuPopupHelper(view.context, popup.menu as MenuBuilder, view)
        menuPopupHelper!!.gravity = Gravity.END
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
        fun onShare(item: String)
        fun onCopy(item: String)
        fun onFavoriteClick(item: QuoteItem)
    }

    companion object {
        private const val FAVORITE_MENU_ITEM_ID = 863
    }
}

