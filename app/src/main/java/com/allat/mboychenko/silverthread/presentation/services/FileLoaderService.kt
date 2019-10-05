package com.allat.mboychenko.silverthread.presentation.services

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.BooksConstants
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.allat.mboychenko.silverthread.domain.interactor.FileLoadingDetailsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.BOOKS_FOLDER_NAME
import com.allat.mboychenko.silverthread.presentation.helpers.WEB_DOWNLOADS_FOLDER_NAME
import com.allat.mboychenko.silverthread.presentation.helpers.getPublicDownloadsStorageDir
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.BOOKS_UPDATE_ACTION_CANCELLED_LOADING
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.BOOKS_UPDATE_ACTION_LOADED
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.BOOKS_UPDATE_ACTION_START_LOADING_FILENAME
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.BOOKS_UPDATE_ACTION_START_LOADING_ID
import com.allat.mboychenko.silverthread.presentation.presenters.BooksPresenter.Companion.BOOKS_UPDATE_BROADCAST_ACTION
import org.koin.android.ext.android.inject
import java.io.File

class FileLoaderService : JobIntentService() {

    private val filesLoadingDetailsStorage: FileLoadingDetailsStorage by inject()
    private val booksHelper: BooksHelper by inject()
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val downloadManager by lazy { getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    override fun onHandleWork(intent: Intent) {
        val url = intent.getStringExtra(FILE_URL_ARG)
        val fileName = intent.getStringExtra(FILE_NAME_ARG)
        val path = intent.getStringExtra(DIR_PATH_ARG)
        val action = intent.getStringExtra(ACTION_ARG)
        val cancelId = intent.getLongExtra(DOWNLOAD_ID_ARG, -1)
        val webDownload = intent.getBooleanExtra(WEB_DOWNLOAD_ARG, false)
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)

        when {
            action?.equals(ACTION_CANCEL_DOWNLOAD) == true -> {
                if (cancelId != -1L) {
                    val loadingIds = filesLoadingDetailsStorage.getLoadingIds()
                    downloadManager.remove(cancelId)
                    loadingIds.entries.find { it.value == cancelId }?.let {
                        finishLoading(it.key, cancelId, cancel = true)
                    }
                }
            }
            action?.equals(ACTION_REFRESH_STATE) == true -> {
                val loadingIds = filesLoadingDetailsStorage.getLoadingIds()
                for (entry in loadingIds) {
                    updateLoadingStatus(entry.key, entry.value)
                }
            }
            else -> url?.let {
                var inProgress = urlInProgressId(it)

                if (inProgress && webDownload) {
                    val loadingId =
                        (filesLoadingDetailsStorage.getLoadingIds()[it] as Int?)?.toLong()
                    loadingId?.let {
                        updateLoadingStatus(url, loadingId)
                        inProgress = urlInProgressId(url)
                    }
                }

                if (!inProgress && !path.isNullOrEmpty() && !fileName.isNullOrEmpty()) {
                    loadFile(it, path, fileName, webDownload)
                }
            }
        }
    }

    private fun urlInProgressId(url: String): Boolean {
        return filesLoadingDetailsStorage.getLoadingIds()[url] != null
    }


    private fun updateLoadingStatus(url: String, id: Long) {
        val query = DownloadManager.Query().apply {
            setFilterById(id)
        }
        val c = downloadManager.query(query)
        if (c.moveToFirst()) {
            when (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING -> {
                    //continue
                }

                DownloadManager.STATUS_SUCCESSFUL,
                DownloadManager.STATUS_FAILED -> {
                    finishLoading(
                        url,
                        id,
                        c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)),
                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    )
                }
            }
        } else {
            finishLoading(url, id)
        }
    }

    private fun finishLoading(
        url: String,
        id: Long,
        status: Int = -1,
        localFileUri: String? = null,
        cancel: Boolean = false
    ) {
        filesLoadingDetailsStorage.removeIdFromLoadings(id)

        if (!cancel) {
            if (localFileUri != null) {
                Uri.parse(localFileUri).path?.let {
                    fileRemoveDownloadExt(File(it))
                }
            } else {
                val files: List<File>
                val fileName: String
                val book = booksHelper.getBookByUrl(url)

                if (book != BooksConstants.EMPTY) {
                    fileName = book.fileName
                    val directoryBooks = getPublicDownloadsStorageDir(BOOKS_FOLDER_NAME)
                    files = directoryBooks?.listFiles().orEmpty().asList()
                } else {
                    fileName = URLUtil.guessFileName(url, null, null)
                    val directoryWeb = getPublicDownloadsStorageDir(WEB_DOWNLOADS_FOLDER_NAME)
                    files = directoryWeb?.listFiles().orEmpty().asList()
                }

                if (files.isNotEmpty()) {
                    files.filter { !it.isDirectory && it.name.contains(fileName) }
                        .forEach { fileRemoveDownloadExt(it) }
                }
            }
        }

        notifyFilesObserverRefresh()
        notifyBooksObserverIfNeed(url, status)
    }

    private fun fileRemoveDownloadExt(file: File) {
        if (file.path.endsWith(DOWNLOADING_EXT)) {
            val path = file.path
            file.renameTo(File(path.substring(0..(path.length - 1) - DOWNLOADING_EXT.length)))
        }
    }

    private fun notifyFilesObserverRefresh() {
        localBroadcastManager.sendBroadcast(Intent(FILES_STATUS_UPDATED_BROADCAST_ACTION))
    }

    private fun notifyBooksObserverIfNeed(url: String, status: Int) {
        val book = booksHelper.getBookByUrl(url)
        if (book != BooksConstants.EMPTY) {
            val fileName = book.fileName
            val extraKeyAction = if (status == DownloadManager.STATUS_SUCCESSFUL) {
                BOOKS_UPDATE_ACTION_LOADED
            } else {
                BOOKS_UPDATE_ACTION_CANCELLED_LOADING
            }

            localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
                .apply { putExtra(extraKeyAction, fileName) })
        }
    }

    private fun loadFile(
        url: String,
        dirPath: String,
        fileName: String,
        webDownload: Boolean = false
    ) {
        val destinationFileUri = Uri.fromFile(File(dirPath, fileName + DOWNLOADING_EXT))
        val notificationVisibility =
            if (!webDownload) DownloadManager.Request.VISIBILITY_VISIBLE else DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription(applicationContext.getString(R.string.downloading))
            .setNotificationVisibility(notificationVisibility)
            .setDestinationUri(destinationFileUri)
            .setAllowedOverMetered(true)

        if (webDownload) {
            request.allowScanningByMediaScanner()
        }

        val downloadID = downloadManager.enqueue(request)

        filesLoadingDetailsStorage.putLoadingId(url, downloadID)
        localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
            .apply {
                putExtra(BOOKS_UPDATE_ACTION_START_LOADING_ID, downloadID)
                putExtra(BOOKS_UPDATE_ACTION_START_LOADING_FILENAME, fileName)
            })
    }

    companion object {
        fun commandLoadFile(
            context: Context,
            url: String,
            dirPath: String,
            fileName: String,
            webDownload: Boolean = false
        ) {
            val intent = Intent()
            intent.putExtra(ACTION_ARG, ACTION_LOAD)
            intent.putExtra(FILE_URL_ARG, url)
            intent.putExtra(FILE_NAME_ARG, fileName)
            intent.putExtra(DIR_PATH_ARG, dirPath)
            intent.putExtra(WEB_DOWNLOAD_ARG, webDownload)
            enqueueWork(context, FileLoaderService::class.java, JOB_ID, intent)
        }

        fun commandCancelLoading(context: Context, id: Long) {
            val intent = Intent()
            intent.putExtra(ACTION_ARG, ACTION_CANCEL_DOWNLOAD)
            intent.putExtra(DOWNLOAD_ID_ARG, id)
            enqueueWork(context, FileLoaderService::class.java, JOB_ID, intent)
        }

        fun commandRefreshLoadings(context: Context) {
            val intent = Intent()
            intent.putExtra(ACTION_ARG, ACTION_REFRESH_STATE)
            enqueueWork(context, FileLoaderService::class.java, JOB_ID, intent)
        }

        private const val JOB_ID = 73644

        const val FILE_URL_ARG = "FILE_URL_ARG"
        const val FILE_NAME_ARG = "FILE_NAME_ARG"
        const val DIR_PATH_ARG = "DIR_PATH_ARG"
        const val WEB_DOWNLOAD_ARG = "WEB_DOWNLOAD_ARG"
        const val DOWNLOAD_ID_ARG = "DOWNLOAD_ID_ARGUMENT"

        const val DOWNLOADING_EXT = ".downloading"

        const val ACTION_ARG = "ACTION_ARG"
        const val ACTION_LOAD = "ACTION_LOAD"
        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"
        const val ACTION_REFRESH_STATE = "ACTION_REFRESH_STATE"

        const val FILES_STATUS_UPDATED_BROADCAST_ACTION = "FILES_STATUS_UPDATED_BROADCAST_ACTION"

    }
}
