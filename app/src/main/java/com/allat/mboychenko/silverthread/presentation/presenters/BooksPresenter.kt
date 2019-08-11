package com.allat.mboychenko.silverthread.presentation.presenters

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.views.listitems.BookItem
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService.Companion.BOOKS_UPDATE_BROADCAST_ACTION
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService.Companion.BOOKS_UPDATE_ACTION_CANCELLED_LOADING
import com.allat.mboychenko.silverthread.presentation.views.fragments.IBooksFragmentView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.lang.StringBuilder


class BooksPresenter(
    private val context: Context,
    private val storage: BooksLoaderDetailsStorage,
    private val booksHelper: BooksHelper
) : BasePresenter<IBooksFragmentView>() {

    override fun attachView(view: IBooksFragmentView) {
        super.attachView(view)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(booksLoadingReceiver, IntentFilter(BOOKS_UPDATE_BROADCAST_ACTION))
        context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        FileLoaderService.commandRefreshLoadings(context)
    }

    override fun detachView() {
        super.detachView()
        LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(booksLoadingReceiver)
        context.unregisterReceiver(onDownloadComplete)
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
            val loadings = storage.getBooksLoadingIds()

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
        manageAddToSubscription(
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
        if (isExternalStorageAvailable().not()) {
            Toast.makeText(context, "Storage not available for writing", Toast.LENGTH_LONG).show()
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
            Toast.makeText(context, "Not enough memory", Toast.LENGTH_LONG).show()
            return
        }

        manageAddToSubscription(
            Observable.fromCallable { getPublicDownloadsStorageDir(BOOKS_FOLDER_NAME) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file ->
                    file?.let {
                        FileLoaderService.commandLoadFile(context, bookUrl, it.path, book.fileName)
                    }
                },
                    {
                        Toast.makeText(context, "Cant create folder for downloads: ${it.message}", Toast.LENGTH_LONG)
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
            Toast.makeText(context, "Storage not available for writing", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(context, "Cant delete book ${bookItem.book.fileName}", Toast.LENGTH_LONG).show()
                    }
                },
                    {
                        Toast.makeText(
                            context, "Cant delete book ${bookItem.book.fileName}, ${it.message}", Toast.LENGTH_LONG
                        ).show()
                    })
        )
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
            val loadedFileName = intent?.getStringExtra(FileLoaderService.BOOKS_UPDATE_ACTION_LOADED)

            val loadingId = intent?.getLongExtra(FileLoaderService.BOOKS_UPDATE_ACTION_START_LOADING_ID, -1)
            val loadingFileName = intent?.getStringExtra(FileLoaderService.BOOKS_UPDATE_ACTION_START_LOADING_FILENAME)

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

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            runTaskOnBackgroundWithResult(
                ExecutorThread.COMPUTATION,
                {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    val bookDownload = storage.getBooksLoadingIds()
                    var url: String? = null
                    for (entry in bookDownload) {
                        if (entry.value == id) {
                            url = entry.key
                            break
                        }
                    }

                    storage.removeIdFromBookLoadings(id)

                    if (url != null) {
                        val book = booksHelper.getBookByUrl(url)
                        val bookFile = getBookUri(book).toFile()
                        if (bookFile.exists().not()) {
                            NO_SUCH_FILE
                        } else {
                            book.fileName
                        }
                    } else {
                        NO_SUCH_FILE
                    }
                },
                { fileName ->
                    if (fileName != NO_SUCH_FILE) {
                        view?.bookLoaded(fileName)
                    }
                })
        }
    }

    companion object {
        const val FILE_SCHEMA = "file://"
        const val BOOKS_FOLDER_NAME = "AllatRa Books"
        const val NO_SUCH_FILE = "NO_SUCH_FILE"
        const val REQUEST_PERMISSION_SAVED_LOAD_FILE_URL = "REQUEST_PERMISSION_SAVED_LOAD_FILE_URL"
        const val REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME = "REQUEST_PERMISSION_SAVED_DELETE_FILE_NAME"
    }

}