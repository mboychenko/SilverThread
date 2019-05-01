package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.dialogs.LanguageSelectionDialogFragment
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.dialogs.LanguageSelectionDialogFragment.Companion.LANGUAGE_SELECTOR_DIALOG_TAG
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.book_item_layout.view.*
import androidx.fragment.app.FragmentActivity
import com.allat.mboychenko.silverthread.R


class BookItem(
    val book: BooksConstants.Book, val bookHelper: BooksHelper,
    var exist: Boolean = false, var loadingId: Int = -1,
    private val bookActionListener: BookActionListener
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        view.imgBackground.setImageResource(book.imageRes)

        initClickListeners(view)

        updateLoadingState(view, exist, loadingId)
    }

    private fun initClickListeners(view: View) {
        view.imgBackground.setOnClickListener {
            if (exist) {
                bookActionListener.onOpen(book)
            } else {
                getLanguageSelectorDialog(view.context, view.context.getString(R.string.select_language_load)) {
                    bookActionListener.onLoad((book.localeDetails[it] ?: error("NoSuchLocale")).url, book.fileName)
                }
            }
        }

        view.buttonLoad.setOnClickListener {
            getLanguageSelectorDialog(view.context, view.context.getString(R.string.select_language_load)) {
                bookActionListener.onLoad((book.localeDetails[it] ?: error("NoSuchLocale")).url, book.fileName)
            }
        }

        view.buttonShare.setOnClickListener {
            getLanguageSelectorDialog(view.context, view.context.getString(R.string.select_language_share)) {
                bookActionListener.onShareLinkClick(
                    bookHelper.getBookTitle(book),
                    (book.localeDetails[it] ?: error("NoSuchLocale")).url
                )
            }
        }
        view.buttonDelete.setOnClickListener {
            getConfirmationDialog(
                view.context,
                bookHelper.getBookTitle(book)
            ) { bookActionListener.onDeleteBook(this) }
        }
    }

    private fun getLanguageSelectorDialog(context: Context, title: String, action: (BooksConstants.BooksLocale) -> Unit) {
        val dialog = LanguageSelectionDialogFragment.newInstance(title, book.localeDetails.keys.toList())
        dialog.setListener(object : LanguageSelectionDialogFragment.LanguageSelectedListener {
            override fun onSelected(locale: BooksConstants.BooksLocale) {
                action(locale)
            }

        })
        dialog.show((context as FragmentActivity).supportFragmentManager, LANGUAGE_SELECTOR_DIALOG_TAG)
    }

    private fun getConfirmationDialog(context: Context, title: String, action: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_confirmation, title))
            .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                action()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateLoadingState(view: View, exist: Boolean, loadingId: Int) {
        if (exist) {
            view.imgBackground.alpha = 1f
            view.buttonDelete.visibility = View.VISIBLE
            view.buttonShare.visibility = View.VISIBLE
            view.buttonLoad.visibility = View.GONE
            loadingBarState(view, false)

        } else {
            view.imgBackground.alpha = 0.3f
            view.buttonDelete.visibility = View.GONE
            view.buttonShare.visibility = View.GONE
            view.buttonLoad.visibility = View.VISIBLE

            if (loadingId != -1) {
                loadingBarState(view, true)
            } else {
                loadingBarState(view, false)
            }
        }
    }

    private fun loadingBarState(view: View, active: Boolean) {
        if (active) {
            view.downloading.visibility = View.VISIBLE
            view.downloadingText.visibility = View.VISIBLE
        } else {
            view.downloading.visibility = View.GONE
            view.downloadingText.visibility = View.GONE
        }
    }

    fun cancelLoading() {
        loadingId = -1
    }

    fun bookLoaded() {
        loadingId = -1
        exist = true
//        updateLoadingState()
        //todo check update state
    }

    override fun getLayout(): Int = R.layout.book_item_layout

    interface BookActionListener {
        fun onShareLinkClick(bookTitle: String, url: String)
        fun onDeleteBook(book: BookItem)
        fun onLoad(url: String, fileName: String)
        fun onCancelLoading(downloadId: Int)
        fun onOpen(book: BooksConstants.Book)
    }
}