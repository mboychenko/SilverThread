package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.listitems.BookItem
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService.Companion.BOOKS_UPDATE_BROADCAST_ACTION
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService.Companion.BOOKS_UPDATE_ACTION_CANCELLED_LOADING
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService.Companion.BOOKS_UPDATE_ACTION_LOADED_FILE_NAME
import com.allat.mboychenko.silverthread.presentation.views.fragments.IBooksFragmentView
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.lang.StringBuilder


class BooksPresenter(
    private val context: Context,
    private val storage: BooksLoaderDetailsStorage,
    private val booksHelper: BooksHelper
) : BasePresenter<IBooksFragmentView>() {

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: IBooksFragmentView) {
        super.attachView(view)
        view.updateItems(getBooksItems())

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(booksLoadingReceiver, IntentFilter(BOOKS_UPDATE_BROADCAST_ACTION))
    }

    override fun detachView() {
        super.detachView()
        LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(booksLoadingReceiver)
        subscriptions.dispose()
    }

    fun updateBooks(filter: BooksConstants.BooksLocale? = null) {
        view?.updateItems(getBooksItems(filter))
    }

    private fun getBooksItems(filter: BooksConstants.BooksLocale? = null): List<BookItem> {
        if (isExternalStorageReadable()) {
            val loadings = storage.getBooksLoadingIds()

            var books = booksHelper.getAllBooks()

            filter?.let { locale ->
                books = books.filter { it.localeDetails.containsKey(locale) }
            }

            return books.map {

                val exist = File(getBookUri(it).path).exists()
                var loadingId: Int = -1
                if (exist.not()) {
                    loadings.keys
                        .find { url -> it.localeDetails.containsValue(BooksConstants.BookDetails(url)) }
                        ?.let { key -> loadingId = loadings[key] ?: -1 }
                }

                BookItem(book = it, bookHelper = booksHelper, exist = exist, loadingId = loadingId, bookActionListener = booksActionListener)

            }
        } else {
            Toast.makeText(context, "Cant access to storage", Toast.LENGTH_LONG).show()
        }

        return emptyList()
    }

    private fun getBookUri(book: BooksConstants.Book): Uri {
        val stringBuilder = StringBuilder()
            .append(FILE_SCHEMA)
            .append(getPublicDownloadsStorageDir(BOOKS_FOLDER_NAME)?.path)
            .append(File.separatorChar)
            .append(book.fileName)
        return Uri.parse(stringBuilder.toString())
    }

    fun loadBook(bookUrl: String, fileName: String = booksHelper.getBookByUrl(bookUrl).fileName) {
        subscriptions.add(
            hasInternetAccess(context) { hasInternet ->
                if (hasInternet) {
                    loadBookContinue(bookUrl, fileName)
                } else {
                    Toast.makeText(context, "Internet not available", Toast.LENGTH_LONG).show()
                    view?.bookLoadingCancelled(fileName)
                }
            }
        )
    }

    private fun loadBookContinue(bookUrl: String, fileName: String) {
        if (isExternalStorageWritable().not()) {
            Toast.makeText(context, "Storage not available for writing", Toast.LENGTH_LONG).show()
            view?.bookLoadingCancelled(fileName)
            return
        }

        if (writeExStoragePermissionGranted(context).not()) {
            view?.bookLoadingCancelled(fileName)
            requestWritePermission(loadBookUrl = bookUrl)
            return
        }

        val book = booksHelper.getBookByUrl(bookUrl)
        val bookSize = book.localeDetails.values.find { it.url == bookUrl }?.bookSize ?: 0

        if (bookSize >= getExternalStorageAvailableSpaceKb()) {
            view?.bookLoadingCancelled(fileName)
            Toast.makeText(context, "Not enough memory", Toast.LENGTH_LONG).show()
            return
        }

        getPublicDownloadsStorageDir(BOOKS_FOLDER_NAME)?.let {
            FileLoaderService.commandLoadFile(context, bookUrl, it.path, book.fileName)
        }
    }

    fun deleteBook(bookItem: BookItem?) {
        if (bookItem == null) {
            return
        }

        if (isExternalStorageWritable().not()) {
            Toast.makeText(context, "Storage not available for writing", Toast.LENGTH_LONG).show()
            return
        }

        if (writeExStoragePermissionGranted(context).not()) {
            requestWritePermission(deleteFileName = bookItem.book.fileName)
            return
        }

        val bookFile = getBookUri(bookItem.book).toFile()

        if (bookFile.exists() && bookFile.delete()) {
            storage.removeLastBookPage(bookItem.book.fileName)
            view?.bookRemoved(bookItem)
        } else {
            Toast.makeText(context, "Cant delete book ${bookItem.book.fileName}", Toast.LENGTH_LONG).show()
        }

    }

    private fun requestWritePermission(loadBookUrl: String? = null, deleteFileName: String? = null) {
        loadBookUrl?.let {
            storage.requestPermissionSaveData(REQUEST_PERMISSION_SAVED_LOAD_FILE_URL, it)
        }

        deleteFileName?.let {
            storage.requestPermissionSaveData(REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME, it)
        }
        view?.requestStoragePermission()
    }

    fun requestPermissionRestoreData(key: String): String? = storage.requestPermissionRestoreData(key)

    fun requestPermissionRemoveData() {
        storage.requestPermissionRemoveData(REQUEST_PERMISSION_SAVED_LOAD_FILE_URL)
        storage.requestPermissionRemoveData(REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME)
    }

    private val booksActionListener = object : BookItem.BookActionListener {

        override fun onShareLinkClick(bookTitle: String, url: String) {
            view?.shareBookLink(bookTitle, url)
        }

        override fun onDeleteBook(book: BookItem) {
            deleteBook(book)
        }

        override fun onLoad(url: String, fileName: String) {
            loadBook(url, fileName)
        }

        override fun onCancelLoading(downloadId: Int) {
            FileLoaderService.commandCancelLoading(context, downloadId)
        }

        override fun onOpen(book: BooksConstants.Book) {
            view?.openBook(book.fileName, getBookUri(book))
        }
    }

    private val booksLoadingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val loadedFileName = intent?.getStringExtra(BOOKS_UPDATE_ACTION_LOADED_FILE_NAME)

            val cancelledFileName = intent?.getStringExtra(BOOKS_UPDATE_ACTION_CANCELLED_LOADING)

            val loadingId = intent?.getIntExtra(FileLoaderService.BOOKS_UPDATE_ACTION_START_LOADING_ID, -1)
            val loadingFileName = intent?.getStringExtra(FileLoaderService.BOOKS_UPDATE_ACTION_START_LOADING_FILENAME)

            if (loadingId != -1 && !loadingFileName.isNullOrEmpty()) {
               view?.loadingStarted(loadingFileName, loadingId!!)
            }

            loadedFileName?.let {
                view?.bookLoaded(it)
            }

            cancelledFileName?.let {
                view?.bookLoadingCancelled(it)
            }

        }
    }

    companion object {
        const val FILE_SCHEMA = "file://"
        const val BOOKS_FOLDER_NAME = "AllatRa Books"
        const val REQUEST_PERMISSION_SAVED_LOAD_FILE_URL = "REQUEST_PERMISSION_SAVED_LOAD_FILE_URL"
        const val REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME = "REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME"
    }

}