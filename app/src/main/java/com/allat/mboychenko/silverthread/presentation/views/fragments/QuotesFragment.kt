package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.animation.AnimatorSet
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_quotes_list.*
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.px
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_quotes_list.view.*
import java.util.*
import java.util.concurrent.TimeUnit


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

        view.fabCenter.setOnClickListener {
            if (notifSettingsFab.visibility == View.VISIBLE) {
                hideFabs()
            } else {
                showFabs()
            }
        }

        view.notifSettingsFab.setOnClickListener { fabActionAndHide(::randomQuote) }
        view.randomQuoteFab.setOnClickListener { fabActionAndHide(::randomQuote) }

        with(view.quotesList) {
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

        quotesSection.update(quotes.map { QuoteItem(it) })

        return view
    }

    private fun randomQuote() {
        val quote = quotes[Random().nextInt(quotes.size)]
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(quote).setTitle(R.string.r_quote).setNegativeButton(
            R.string.hide
        ) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun fabActionAndHide(action: () -> Unit) {
        action()
        hideFabs()
    }

    private fun hideFabs() {
        notifSettingsFab.animateFabHide()
        randomQuoteFab.animateFabHide()
    }

    private fun showFabs() {
        notifSettingsFab.animateFabOpen(270f)
        randomQuoteFab.animateFabOpen(340f)
    }

    private fun FloatingActionButton.animateFabHide() {
        val lp = layoutParams as ConstraintLayout.LayoutParams
        val animRadius = ValueAnimator.ofInt(80.px, 0)
        animRadius.doOnEnd { visibility = View.GONE }

        animRadius.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            lp.circleRadius = `val`
            layoutParams = lp
        }

        animRadius.duration = 200
        animRadius.interpolator = LinearInterpolator()
        animRadius.start()
    }

    private fun FloatingActionButton.animateFabOpen(angle: Float) {
        val lp = layoutParams as ConstraintLayout.LayoutParams

        val animRadius = ValueAnimator.ofInt(0, 80.px)
        val animAngle = ValueAnimator.ofFloat(0f, angle)
        animRadius.doOnStart { visibility = View.VISIBLE }

        animRadius.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            lp.circleRadius = `val`
            layoutParams = lp
        }

        animAngle.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Float
            lp.circleAngle = `val`
            layoutParams = lp
        }

        AnimatorSet()
            .apply {
                playTogether(animRadius, animAngle)
                duration = TimeUnit.SECONDS.toMillis(1)
                interpolator = LinearInterpolator()
                start()
            }


    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: String)
    }

    companion object {
        const val QUOTES_FRAGMENT_TAG = "QUOTES_FRAGMENT_TAG"
    }
}
