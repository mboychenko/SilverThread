package com.allat.mboychenko.silverthread.presentation.views.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.presentation.views.dialogs.LanguageSelectionDialogFragment
import com.allat.mboychenko.silverthread.presentation.views.dialogs.LanguageSelectionDialogFragment.Companion.LANGUAGE_SELECTOR_DIALOG_TAG
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.book_item_layout.view.*
import androidx.fragment.app.FragmentActivity
import com.allat.mboychenko.silverthread.R


class BookItem(
    val book: BooksConstants.Book, val bookHelper: BooksHelper,
    var exist: Boolean = false, var loadingId: Long = -1,
    private val bookActionListener: BookActionListener
) : Item() {

    var viewRef: View? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewRef = viewHolder.itemView

        viewRef!!.imgBackground.setImageResource(book.imageRes)

        updateLoadingState(viewRef!!, exist, loadingId)

        initClickListeners(viewRef!!)
    }

    private fun initClickListeners(view: View) {
        view.imgBackground.setOnClickListener {
            if (exist) {
                bookActionListener.onOpen(book)
            } else if (loadingId == -1L) {
                getLanguageSelectorDialog(view.context, view.context.getString(R.string.select_language_load)) {
                    loadingBarState(view, true)
                    bookActionListener.onLoad((book.localeDetails[it] ?: error("NoSuchLocale")).url, book.fileName)
                }
            }
        }

        view.cancelLoading.setOnClickListener {
            if (loadingId != -1L) {
                bookActionListener.onCancelLoading(loadingId)
            }
        }

        view.buttonLoad.setOnClickListener {
            if (!exist && loadingId == -1L) {
                getLanguageSelectorDialog(view.context, view.context.getString(R.string.select_language_load)) {
                    loadingBarState(view, true)
                    bookActionListener.onLoad((book.localeDetails[it] ?: error("NoSuchLocale")).url, book.fileName)
                }
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

    private fun updateLoadingState(view: View, exist: Boolean, loadingId: Long) {
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

            if (loadingId != -1L) {
                loadingBarState(view, true)
            } else {
                loadingBarState(view, false)
            }
        }
    }

    private fun loadingBarState(view: View, active: Boolean) {
        if (active) {
            view.loadingClickGrabber.visibility = View.VISIBLE
            view.downloading.visibility = View.VISIBLE
            view.downloadingText.visibility = View.VISIBLE
            view.cancelLoading.visibility = View.VISIBLE
            view.buttonLoad.visibility = View.GONE
        } else {
            view.loadingClickGrabber.visibility = View.GONE
            view.downloading.visibility = View.GONE
            view.downloadingText.visibility = View.GONE
            view.cancelLoading.visibility = View.GONE
        }
    }

    fun loadingStarted(id: Long) {
        loadingId = id
        viewRef?.let { loadingBarState(it, true) }
    }

    fun cancelLoading() {
        loadingId = -1
        notifyChanged()
    }

    fun bookLoaded() {
        loadingId = -1
        exist = true
        notifyChanged()
    }

    fun deleteBook() {
        exist = false
        notifyChanged()
    }

    override fun getLayout(): Int = R.layout.book_item_layout

    interface BookActionListener {
        fun onShareLinkClick(bookTitle: String, url: String)
        fun onDeleteBook(book: BookItem)
        fun onLoad(url: String, fileName: String)
        fun onCancelLoading(downloadId: Long)
        fun onOpen(book: BooksConstants.Book)
    }
}