package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.presentation.views.listitems.BookItem
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.REQUEST_PERMISSION_SAVED_LOAD_FILE_URL
import com.allat.mboychenko.silverthread.presentation.views.activities.BookReaderActivity
import com.allat.mboychenko.silverthread.presentation.views.activities.BookReaderActivity.Companion.BOOK_NAME_ARG
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_books_list.*
import kotlinx.android.synthetic.main.fragment_books_list.view.*
import org.koin.android.ext.android.inject

class BooksFragment: Fragment(), IAllatRaFragments, IBooksFragmentView {

    private val presenter : BooksPresenter by inject()
    private val booksItemsSection = Section()

    override fun getFragmentTag(): String = BOOKS_FRAGMENT_TAG

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_books_list, container, false)

        with(view.booksList) {
            layoutManager = GridLayoutManager(context, 2)
            adapter = GroupAdapter<ViewHolder>().apply {
                add(booksItemsSection)
            }
        }

        with(view.languageFilter) {
            adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                listOf(context.getString(R.string.all)) + BooksConstants.getLocales().map { it.language })
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) {    //All
                        presenter.updateBooks()
                    } else {
                        presenter.updateBooks(BooksConstants.getLocales()
                            .find { it.language == adapter.getItem(position) })
                    }
                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun updateItems(items: List<BookItem>) {
        booksItemsSection.update(items)
        booksItemsSection.notifyChanged()
    }

    override fun bookRemoved(book: BookItem) {
        book.deleteBook()
    }

    override fun bookLoaded(fileName: String) {
        val bookItem = getBookItem(fileName)
        bookItem?.exist = true
        bookItem?.bookLoaded()
    }

    override fun loadingStarted(fileName: String, loadingId: Long) {
        val bookItem = getBookItem(fileName)
        bookItem?.loadingStarted(loadingId)
    }

    override fun bookLoadingCancelled(fileName: String) {
        val bookItem = getBookItem(fileName)
        bookItem?.cancelLoading()
    }

    override fun notifyBooksUpdate() {
        booksItemsSection.notifyChanged()
    }

    override fun requestStoragePermission() {
        try {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e("Permission request fail", e.message)
        }
    }

    override fun openBook(name: String, bookUri: Uri) {
        startActivity(
            Intent(context, BookReaderActivity::class.java)
                .apply {
                    data = bookUri
                    putExtra(BOOK_NAME_ARG, name)
                }
        )
    }

    override fun shareBookLink(bookTitle: String, bookUrl: String) {
        shareText(context, getString(R.string.link_to_load, bookTitle, bookUrl), getString(R.string.share_link, bookTitle))
    }

    override fun showLoading() {
        loadingContainer.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loadingContainer.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for ((position, permission) in permissions.withIndex()) {
                val grantResult = grantResults[position]

                if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {

                        val loadFileUrl = presenter.requestPermissionRestoreData(REQUEST_PERMISSION_SAVED_LOAD_FILE_URL)
                        val deleteFileName = presenter.requestPermissionRestoreData(REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME)

                        loadFileUrl?.let { presenter.loadBook(it) }
                        deleteFileName?.let { presenter.deleteBook(getBookItem(it)) }

                    } else {
                        presenter.requestPermissionRemoveData()
                        Toast.makeText(context, "permission not granted", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }

    }

    private fun getBookItem(bookName: String): BookItem? {
        for (i in 0..booksItemsSection.itemCount) {
            val bookItem = booksItemsSection.getItem(i) as BookItem
            if (bookItem.book.fileName == bookName) {
                return bookItem
            }
        }
        return null
    }

    companion object {
        const val BOOKS_FRAGMENT_TAG = "BOOKS_FRAGMENT_TAG"
        private const val PERMISSION_REQUEST_CODE = 986
    }

}