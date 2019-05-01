package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.listitems.BookItem
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.REQUEST_PERMISSION_SAVED_LOAD_FILE_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
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

        presenter.attachView(this)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun updateItems(items: List<BookItem>) = booksItemsSection.update(items)

    override fun bookRemoved(book: BookItem) {
        booksItemsSection.remove(book)
    }

    override fun bookLoaded(fileName: String) {
        val bookItem = getBookItem(fileName)
        bookItem?.exist = true
        bookItem?.bookLoaded()
        bookItem?.notifyChanged() //todo check if need call
    }

    override fun bookLoadingCancelled(fileName: String) {
        val bookItem = getBookItem(fileName)
        bookItem?.cancelLoading()
        bookItem?.notifyChanged() //todo check if need call
    }

    override fun notifyBooksUpdate() {
        booksItemsSection.notifyChanged()
    }

    override fun requestStoragePermission() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),        //Manifest.permission.READ_EXTERNAL_STORAGE,
            PERMISSION_REQUEST_CODE
        )
    }

    override fun shareBookLink(bookTitle: String, bookUrl: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, bookTitle)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, bookUrl)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_link, bookTitle)))
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
        private const val PERMISSION_REQUEST_CODE = 0x16523
    }

}