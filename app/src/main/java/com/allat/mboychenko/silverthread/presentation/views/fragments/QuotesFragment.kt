package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.animation.AnimatorSet
import android.os.Bundle
import com.allat.mboychenko.silverthread.R
import kotlinx.android.synthetic.main.fragment_quotes_list.*
import android.animation.ValueAnimator
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.allat.mboychenko.silverthread.presentation.di.QUOTES_FRAGMENT_SCOPE_NAME
import com.allat.mboychenko.silverthread.presentation.views.dialogs.QuotesNotificationSettingsDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.QuotesNotificationSettingsDialog.Companion.QUOTES_NOTIF_SETTINGS_DIALOG_TAG
import com.allat.mboychenko.silverthread.presentation.helpers.px
import com.allat.mboychenko.silverthread.presentation.presenters.ListFavoritePresenter
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesPresenter
import com.allat.mboychenko.silverthread.presentation.views.dialogs.RandomQuoteDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.RandomQuoteDialog.Companion.RANDOM_QUOTE_DIALOG_TAG
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_quotes_list.view.*
import org.koin.android.ext.android.getKoin
import org.koin.core.qualifier.named

class QuotesFragment : BaseFavoritesSearchFragment() {

    private val quotesFragmentSession = getKoin().getOrCreateScope(QUOTES_FRAGMENT_DI_SCOPE_SESSION, named(QUOTES_FRAGMENT_SCOPE_NAME))
    private val presenter: QuotesPresenter by quotesFragmentSession.inject()

    override fun getFragmentTag(): String = QUOTES_FRAGMENT_TAG

    override fun toolbarTitle(): Int = R.string.quotes

    override fun getPresenter(): ListFavoritePresenter<IListFavFragmentView> = presenter

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

        initList(view.quotesList)

        return view
    }

    override fun onStart() {
        super.onStart()
        showIncomingFromNotificationQuote()
    }

    fun showIncomingFromNotificationQuote(position: Int? = null) {
        val quotePosition = position ?: arguments?.getInt(QUOTES_INCOMING_POSITION, -1)
        if (quotePosition != null && quotePosition != -1) {
            val quote = presenter.getItem(quotePosition)
            showQuote(quotePosition, quote)
        }
    }

    override fun onPause() {
        super.onPause()
        hideFabs()
    }

    override fun onDestroy() {
        super.onDestroy()
        quotesFragmentSession.close()
    }

    private fun notifSettings() {
        QuotesNotificationSettingsDialog()
            .show(parentFragmentManager, QUOTES_NOTIF_SETTINGS_DIALOG_TAG)
    }

    private fun randomQuote() {
        val (position, quote) = presenter.getRandomQuote()
        showQuote(position, quote)
    }

    /**
     * https://stackoverflow.com/questions/27580306/dismissed-dialog-fragment-reappears-again-when-app-is-resumed
     */
    private fun showQuote(position: Int, quote: String) {
        val qDialog = RandomQuoteDialog.newInstance(quote, position)
        qDialog.show(parentFragmentManager, RANDOM_QUOTE_DIALOG_TAG)
    }

    private fun fabActionAndHide(action: () -> Unit) {
        action()
        hideFabs()
    }

    private fun hideFabs(forceHide: Boolean = false) {
        notifSettingsFab.animateFabHide(forceHide)
        randomQuoteFab.animateFabHide(forceHide)
    }

    private fun showFabs() {
        notifSettingsFab.animateFabOpen(270f)
        randomQuoteFab.animateFabOpen(340f)
    }

    private fun FloatingActionButton.animateFabHide(force: Boolean = false) {
        val lp = layoutParams as ConstraintLayout.LayoutParams

        if (lp.circleRadius != 0) {
            if (force) {
                lp.circleRadius = 0
                layoutParams = lp
                visibility = View.INVISIBLE
                return
            }

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
    }

    private fun FloatingActionButton.animateFabOpen(angle: Float) {
        val lp = layoutParams as ConstraintLayout.LayoutParams

        if (lp.circleRadius == 0) {
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
        const val QUOTES_FRAGMENT_DI_SCOPE_SESSION = "quotesFragmentSession"
    }
}
