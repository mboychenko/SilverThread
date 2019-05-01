package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class QuotesFragment : Fragment(), IAllatRaFragments {

    override fun getFragmentTag(): String = QUOTES_FRAGMENT_TAG

    private lateinit var quotes: Array<String>

    private val quotesSection = Section()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quotes_list, container, false)

        quotes = resources.getStringArray(R.array.quotes)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    add(quotesSection)
                    setOnItemClickListener { item, _ ->
                        if (item is QuoteItem) {
                            item
                        }
                    }
                }
            }
        }

        quotesSection.update(quotes.map { QuoteItem(it) })

        return view
    }


    //    @OnClick(R.id.fab)
//    fun fabOnClick() {
//        val quote = quotes[Random().nextInt(quotes.size)]
//        val builder = AlertDialog.Builder(this@MainActivity)
//        builder.setMessage(quote).setTitle(R.string.r_quote).setNegativeButton(
//            R.string.hide
//        ) { dialog, _ -> dialog.dismiss() }
//        val dialog = builder.create()
//        dialog.show()
//    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: String)
    }

    companion object {
        const val QUOTES_FRAGMENT_TAG = "QUOTES_FRAGMENT_TAG"
    }
}
