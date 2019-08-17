package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.animation.AnimatorSet
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_quotes_list.*
import android.animation.ValueAnimator
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import com.allat.mboychenko.silverthread.presentation.views.dialogs.QuotesNotificationSettingsDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.QuotesNotificationSettingsDialog.Companion.QUOTES_NOTIF_SETTINGS_DIALOG_TAG
import com.allat.mboychenko.silverthread.presentation.helpers.px
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesPresenter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_quotes_list.view.*
import org.koin.android.ext.android.inject

class QuotesFragment : BaseAllatRaFragment(), IQuotesFragmentView {

    private val presenter : QuotesPresenter by inject()

    override fun getFragmentTag(): String = QUOTES_FRAGMENT_TAG

    private val quotesSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getInt(QUOTES_SAVE_FAVORITE_STATE_KEY, -1)?.let {
            if (it != -1) {
                presenter.changeQuotesState(QuotesPresenter.QuotesState.values()[it])
            }
        }
        setHasOptionsMenu(true)
    }

    override fun toolbarTitle(): Int = R.string.quotes

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.favorites_menu_item, menu)
        updateFavMenuItemIcon(menu.getItem(0))
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.favorites -> {
                presenter.changeQuotesState()
                updateFavMenuItemIcon(item)
                true
            }
            else -> false
        }
    }

    private fun updateFavMenuItemIcon(item: MenuItem) {
        context?.let {
            if (presenter.getQuotesState() == QuotesPresenter.QuotesState.FAVORITE) {
                item.icon = ContextCompat.getDrawable(it, R.drawable.ic_favorite_black_24dp)
            } else {
                item.icon = ContextCompat.getDrawable(it, R.drawable.ic_favorite_border_black_24dp)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quotes_list, container, false)

        view.fabCenter.setOnClickListener {
            if (notifSettingsFab.visibility == View.VISIBLE) {
                hideFabs()
            } else {
                showFabs()
            }
        }

        view.notifSettingsFab.setOnClickListener { fabActionAndHide(::notifSettings) }
        view.randomQuoteFab.setOnClickListener { fabActionAndHide(::randomQuote) }

        quotesSection.setHideWhenEmpty(true)

        with(view.quotesList) {
            layoutManager = LinearLayoutManager(context)
            adapter = GroupAdapter<ViewHolder>().apply {
                add(quotesSection)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        hideFabs()
        presenter.attachView(this)

        if (quotesSection.itemCount == 0) {
            presenter.getQuotes()
        }

        showIncomingFromNotificationQuote()
    }

    private fun showIncomingFromNotificationQuote() {
        val quotePosition = arguments?.getInt(QUOTES_INCOMING_POSITION, -1)
        if (quotePosition != null && quotePosition != -1) {
            val quote = presenter.getQuote(quotePosition)
            showQuote(quotePosition, quote)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun quotesReady(quotes: List<QuoteItem>) {
        noItemsToShow.visibility = if (quotes.isEmpty()) View.VISIBLE else View.GONE
        quotesSection.update(quotes)
    }

    override fun removeItem(item: QuoteItem) {
        quotesSection.remove(item)
        if (quotesSection.itemCount == 0) {
            noItemsToShow.visibility = View.VISIBLE
        }
    }

    private fun notifSettings() {
        fragmentManager?.let {
            QuotesNotificationSettingsDialog()
                .show(it, QUOTES_NOTIF_SETTINGS_DIALOG_TAG)
        }
    }

    private fun randomQuote() {
        val (position, quote) = presenter.getRandomQuote()
        showQuote(position, quote)
    }

    private fun showQuote(position: Int, quote: String) {
        val builder = AlertDialog.Builder(context!!)
        builder
            .setMessage(quote)
            .setTitle(R.string.r_quote)
            .setNegativeButton(R.string.hide) { dialog, _ -> dialog.dismiss() }
            .setNeutralButton(R.string.in_favorite) { _, _ -> presenter.addToFavorite(position) }
            .setPositiveButton(R.string.share) { _, _ -> shareText(quote, getString(R.string.share_quote)) }
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
        animRadius.doOnEnd { visibility = View.INVISIBLE }

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
        val animAngle = ValueAnimator.ofFloat(200f, angle)
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
                duration = 500
                interpolator = LinearInterpolator()
                start()
            }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(QUOTES_SAVE_FAVORITE_STATE_KEY, presenter.getQuotesState().ordinal)
    }

    companion object {
        fun newInstance(quotePosition: Int): QuotesFragment {
            val dialog = QuotesFragment()
            val args = Bundle().apply {
                putInt(QUOTES_INCOMING_POSITION, quotePosition)
            }
            dialog.arguments = args
            return dialog
        }

        const val QUOTES_FRAGMENT_TAG = "QUOTES_FRAGMENT_TAG"
        const val QUOTES_INCOMING_POSITION = "QUOTES_INCOMING_POSITION"
        const val QUOTES_SAVE_FAVORITE_STATE_KEY = "QUOTES_SAVE_FAVORITE_STATE_KEY"
    }
}
