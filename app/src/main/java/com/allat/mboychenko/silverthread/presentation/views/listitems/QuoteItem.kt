package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.listitems

import com.allat.mboychenko.silverthread.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.quote_item_layout.view.*
class QuoteItem(val quoteText: String) : Item() {

    override fun getLayout(): Int = R.layout.quote_item_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {
        with(viewHolder.itemView) {
            quote.text = quoteText
//            setOnClickListener(mOnClicfkListener)
        }
    }
}