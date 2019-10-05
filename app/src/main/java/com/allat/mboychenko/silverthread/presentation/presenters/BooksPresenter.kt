package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Handler
import android.widget.Toast
import androidx.core.net.toFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.domain.interactor.FileLoadingDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.views.listitems.BookItem
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.presentation.views.fragments.IBooksFragmentView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.lang.StringBuilder


class BooksPresenter(
    private val context: Context,
    private val storage: BooksLoaderDetailsStorage,
    private val loadingDetailsStorage: FileLoadingDetailsStorage,
    private val booksHelper: BooksHelper
) : BasePresenter<IBooksFragmentView>() {

    override fun attachView(view: IBooksFragmentView) {
        super.attachView(view)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(booksLoadingReceiver, IntentFilter(BOOKS_UPDATE_BROADCAST_ACTION))
        updateBooks()
        Handler().postDelayed({ FileLoaderService.commandRefreshLoadings(context) }, 2000)
    }

    override fun detachView() {
        super.detachView()
        LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(booksLoadingReceiver)
    }

    fun updateBooks(filter: BooksConstants.BooksLocale? = null) {
        view?.showLoading()
        manageAddToSubscription(
            Observable.fromCallable { getBooksItems(filter) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    view?.updateItems(it)
                    view?.hideLoading()
                }
        )
    }

    private fun getBooksItems(filter: BooksConstants.BooksLocale? = null): List<BookItem> {
        if (isExternalStorageReadable()) {
            val loadings = loadingDetailsStorage.getLoadingIds()

            var books = booksHelper.getAllBooks()

            filter?.let { locale ->
                books = books.filter { it.localeDetails.containsKey(locale) }
            }

            return books.map {

                var exist = getBookUri(it).toFile().exists()

                var loadingId: Long = -1L
                loadings.keys
                    .find { url -> it.localeDetails.containsValue(BooksConstants.BookDetails(url)) }
                    ?.let { key ->
                        loadingId = try {
                            loadings[key] ?: -1L
                        } catch (e: ClassCastException) {   //weired case
                            @Suppress("CAST_NEVER_SUCCEEDS")
                            (loadings[key] as Int).toLong()
                        }
                        exist = false
                    }

                BookItem(
                    book = it,
                    bookHelper = booksHelper,
                    exist = exist,
                    loadingId = loadingId,
                    bookActionListener = booksActionListener
                )

            }
        } else {
            Toast.makeText(context, R.string.cant_access_storage, Toast.LENGTH_LONG).show()
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
        manageAddToSubscription(
            hasInternetAccess(context) { hasInternet ->
                if (hasInternet) {
                    loadBookContinue(bookUrl, fileName)
                } else {
                    Toast.makeText(context, R.string.internet_not_available, Toast.LENGTH_LONG)
                        .show()
                    view?.bookLoadingCancelled(fileName)
                }
            }
        )
    }

    private fun loadBookContinue(bookUrl: String, fileName: String) {
        if (isExternalStorageAvailable().not()) {
            Toast.makeText(context, R.string.storage_not_available_writing, Toast.LENGTH_LONG)
                .show()
            view?.bookLoadingCancelled(fileName)
            return
        }

        if (extStoragePermissionGranted(context).not()) {
            view?.bookLoadingCancelled(fileName)
            requestWritePermission(loadBookUrl = bookUrl)
            return
        }

        val book = booksHelper.getBookByUrl(bookUrl)
        val bookSize = book.localeDetails.values.find { it.url == bookUrl }?.bookSizeBytes ?: 0

        if (bookSize >= getExternalStorageAvailableSpaceBytes()) {
            view?.bookLoadingCancelled(fileName)
            Toast.makeText(context, R.string.not_enough_memory, Toast.LENGTH_LONG).show()
            return
        }

        manageAddToSubscription(
            Observable.fromCallable { getPublicDownloadsStorageDir(BOOKS_FOLDER_NAME) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file ->
                    file?.let {
                        FileLoaderService.commandLoadFile(
                            context,
                            bookUrl,
                            it.path,
                            book.fileName
                        )
                    }
                },
                    {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.cant_create_folder_for_downloads,
                                it.message
                            ),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                )
        )
    }

    fun deleteBook(bookItem: BookItem?) {
        if (bookItem == null) {
            return
        }

        if (isExternalStorageAvailable().not()) {
            Toast.makeText(context, R.string.storage_not_available_writing, Toast.LENGTH_LONG)
                .show()
            return
        }

        if (extStoragePermissionGranted(context).not()) {
            requestWritePermission(deleteFileName = bookItem.book.fileName)
            return
        }

        manageAddToSubscription(
            Observable.fromCallable {
                val bookFile = getBookUri(bookItem.book).toFile()
                bookFile.exists() && bookFile.delete()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it) {
                        storage.removeLastBookPage(bookItem.book.fileName)
                        view?.bookRemoved(bookItem)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.cant_delete_book,
                                bookItem.book.fileName
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                    {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.cant_delete_book_error,
                                bookItem.book.fileName,
                                it.message
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    })
        )
    }

    private fun requestWritePermission(
        loadBookUrl: String? = null,
        deleteFileName: String? = null
    ) {
        loadBookUrl?.let {
            storage.requestPermissionSaveData(REQUEST_PERMISSION_SAVED_LOAD_FILE_URL, it)
        }

        deleteFileName?.let {
            storage.requestPermissionSaveData(REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME, it)
        }
        view?.requestStoragePermission()
    }

    fun requestPermissionRestoreData(key: String): String? =
        storage.requestPermissionRestoreData(key)

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

        override fun onCancelLoading(downloadId: Long) {
            FileLoaderService.commandCancelLoading(context, downloadId)
        }

        override fun onOpen(book: BooksConstants.Book) {
            view?.openBook(book.fileName, getBookUri(book))
        }
    }

    private val booksLoadingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val cancelledFileName = intent?.getStringExtra(BOOKS_UPDATE_ACTION_CANCELLED_LOADING)
            val loadedFileName = intent?.getStringExtra(BOOKS_UPDATE_ACTION_LOADED)

            val loadingId = intent?.getLongExtra(BOOKS_UPDATE_ACTION_START_LOADING_ID, -1)
            val loadingFileName = intent?.getStringExtra(BOOKS_UPDATE_ACTION_START_LOADING_FILENAME)

            if (loadingId != null && loadingId != -1L && !loadingFileName.isNullOrEmpty()) {
                view?.loadingStarted(loadingFileName, loadingId)

            }

            cancelledFileName?.let {
                view?.bookLoadingCancelled(it)
            }

            loadedFileName?.let {
                view?.bookLoaded(it)
            }

        }
    }

    companion object {
        const val FILE_SCHEMA = "file://"
        const val REQUEST_PERMISSION_SAVED_LOAD_FILE_URL = "REQUEST_PERMISSION_SAVED_LOAD_FILE_URL"
        const val REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME =
            "REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME"

        const val BOOKS_UPDATE_BROADCAST_ACTION = "BOOKS_UPDATE_BROADCAST_ACTION"
        const val BOOKS_UPDATE_ACTION_CANCELLED_LOADING = "BOOKS_UPDATE_ACTION_CANCELLED_LOADING"
        const val BOOKS_UPDATE_ACTION_LOADED = "BOOKS_UPDATE_ACTION_LOADED"
        const val BOOKS_UPDATE_ACTION_START_LOADING_ID = "BOOKS_UPDATE_ACTION_START_LOADING_ID"
        const val BOOKS_UPDATE_ACTION_START_LOADING_FILENAME =
            "BOOKS_UPDATE_ACTION_START_LOADING_FILENAME"
    }

}