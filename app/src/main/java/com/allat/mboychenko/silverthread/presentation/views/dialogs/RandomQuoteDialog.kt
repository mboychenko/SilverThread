package com.allat.mboychenko.silverthread.presentation.views.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.alignRight
import com.allat.mboychenko.silverthread.presentation.presenters.QuotesPresenter
import com.allat.mboychenko.silverthread.presentation.views.fragments.IViewContext
import com.allat.mboychenko.silverthread.presentation.views.fragments.QuotesFragment
import org.koin.android.ext.android.getKoin
import kotlin.properties.Delegates

class RandomQuoteDialog : DialogFragment(), IViewContext {

    private val quotesFragmentSession = getKoin().getScope(QuotesFragment.QUOTES_FRAGMENT_DI_SCOPE_SESSION)
    private val presenter: QuotesPresenter by quotesFragmentSession.inject()

    private var quote: String = ""
    private var quotePosition: Int = -1
    private var favorite: Boolean by Delegates.observable(false, {_, old, new ->
        if (old != new)
        updateFavIcon()
    })

    private lateinit var favIcon: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        return object : Dialog(context!!, theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }
    }

    override fun getViewContext(): Context? {
        return context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.random_quote_dialog, container, false)

        arguments?.let { arg ->
            quote = arg.getString(DIALOG_QUOTE_ARG, "")
            quotePosition = arg.getInt(DIALOG_QUOTE_POSITION_ARG, -1)

            val styledResultText = SpannableString(quote)
            quote.indexOf("\n").takeIf { it > -1 }
                ?.let { styledResultText.alignRight(it, quote.length) }

            val desc = view.findViewById<TextView>(R.id.description)
            desc.movementMethod = ScrollingMovementMethod()
            desc.text = styledResultText
        }

        favIcon = view.findViewById(R.id.fav_icon)

        favIcon.setOnClickListener {
            if (favorite) {
                presenter.removeFromFavorite(quotePosition)
            } else {
                presenter.addToFavorite(quotePosition)
            }
            favorite = favorite.not()
        }

        view.findViewById<Button>(R.id.share)?.setOnClickListener {
            shareText(quote, getString(R.string.share_quote))
            dismiss()
        }

        view.findViewById<Button>(R.id.hide)?.setOnClickListener { dismiss() }

        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onStart() {
        super.onStart()
        presenter.isQuoteInFav(quotePosition) { fav ->
            favorite = fav
        }
    }

    private fun updateFavIcon() {
        context?.let { ctx ->
            favIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    ctx,
                    if (favorite) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.dispose()
    }

    companion object {

        fun newInstance(quote: String, position: Int): RandomQuoteDialog {
            val dialog = RandomQuoteDialog()
            dialog.arguments = Bundle().apply {
                putString(DIALOG_QUOTE_ARG, quote)
                putInt(DIALOG_QUOTE_POSITION_ARG, position)
            }
            return dialog
        }

        const val DIALOG_QUOTE_ARG = "DIALOG_QUOTE_ARG"
        const val DIALOG_QUOTE_POSITION_ARG = "DIALOG_QUOTE_POSITION_ARG"
        const val RANDOM_QUOTE_DIALOG_TAG = "RANDOM_QUOTE_DIALOG_TAG"
    }
}