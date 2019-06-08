package com.allat.mboychenko.silverthread.presentation.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import com.downloader.*
import com.downloader.Status.QUEUED
import com.downloader.Status.CANCELLED
import com.downloader.Status.COMPLETED
import com.downloader.Status.PAUSED
import com.downloader.Status.RUNNING
import com.downloader.Status.UNKNOWN
import com.downloader.Error
import org.koin.android.ext.android.inject

class FileLoaderService : JobIntentService() {

    private val storage: BooksLoaderDetailsStorage by inject()
    private val booksHelper: BooksHelper by inject()
    private lateinit var localBroadcastManager: LocalBroadcastManager

    override fun onHandleWork(intent: Intent) {
        val url = intent.getStringExtra(FILE_URL_ARG)
        val fileName = intent.getStringExtra(FILE_NAME_ARG)
        val path = intent.getStringExtra(DIR_PATH_ARG)
        val action = intent.getStringExtra(ACTION_ARG)
        val cancelId = intent.getIntExtra(DOWNLOAD_ID_ARG, -1)
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)

        when {
            action?.equals(ACTION_CANCEL_DOWNLOAD) == true -> {
                if (cancelId != -1) {
                    PRDownloader.cancel(cancelId)
                    storage.getBooksLoadingIds().entries.find { it.value == cancelId }?.key?.let {
                        updateLoadingStatus(it, cancelId)
                    }
                }
            }
            action?.equals(ACTION_REFRESH_STATE) == true -> {
                val loadingIds = storage.getBooksLoadingIds()

                for (entry in loadingIds) {
                    updateLoadingStatus(entry.key, entry.value)
                }
            }
            else -> url?.let {
                val loadingId = urlInProgressId(it)
                if (loadingId != -1) {
                    updateLoadingStatus(it, loadingId)
                } else if (path.isNullOrEmpty().not() && fileName.isNullOrEmpty().not()) {
                    loadFile(it, path!!, fileName!!)
                }
            }
        }
    }

    private fun urlInProgressId(url: String): Int {
        return storage.getBooksLoadingIds()[url] ?: -1
    }


    private fun updateLoadingStatus(url: String, id: Int) {
        val status = PRDownloader.getStatus(id)
        when (status!!) {
            PAUSED -> resumeLoading(id)

            COMPLETED -> bookLoaded(url, id)

            CANCELLED,
            UNKNOWN -> canceledLoading(url, id)

            QUEUED,
            RUNNING -> Log.d(TAG, "$id loading in progress")
        }
    }

    private fun bookLoaded(url: String, id: Int) {
        storage.removeIdFromBookLoadings(id)
        val fileName = booksHelper.getBookByUrl(url).fileName
        localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
            .apply { putExtra(BOOKS_UPDATE_ACTION_LOADED_FILE_NAME, fileName) })
    }

    private fun canceledLoading(url: String, id: Int) {
        storage.removeIdFromBookLoadings(id)
        val fileName = booksHelper.getBookByUrl(url).fileName
        localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
            .apply { putExtra(BOOKS_UPDATE_ACTION_CANCELLED_LOADING, fileName) })
        PRDownloader.cancel(id)
    }

    private fun resumeLoading(id: Int) {
        PRDownloader.resume(id)
    }

    private fun loadFile(url: String, dirPath: String, fileName: String) {
        val downloadId = PRDownloader.download(url, dirPath, fileName)
            .build()
            .start(object : OnDownloadListener {
                override fun onError(error: Error) {
                    Log.e(TAG, "loading error")
                    fireUpdateBooksStatus()
                }

                override fun onDownloadComplete() {
                    fireUpdateBooksStatus()
                }

            })

        storage.putBookLoadingId(url, downloadId)
        localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
            .apply {
                putExtra(BOOKS_UPDATE_ACTION_START_LOADING_ID, downloadId)
                putExtra(BOOKS_UPDATE_ACTION_START_LOADING_FILENAME, fileName)
            })
    }

    private fun fireUpdateBooksStatus() {
        val bookDownload = storage.getBooksLoadingIds()
        for (download in bookDownload) {
            updateLoadingStatus(download.key, download.value)
        }
    }

    companion object {
        fun commandLoadFile(context: Context, url: String, dirPath: String, fileName: String) {
            val intent = Intent()
            intent.putExtra(ACTION_ARG, ACTION_LOAD)
            intent.putExtra(FILE_URL_ARG, url)
            intent.putExtra(FILE_NAME_ARG, fileName)
            intent.putExtra(DIR_PATH_ARG, dirPath)
            enqueueWork(context, FileLoaderService::class.java, JOB_ID, intent)
        }

        fun commandCancelLoading(context: Context, id: Int) {
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

        const val TAG = "LOADING_SERVICE_TAG"
        const val JOB_ID = 101


        const val FILE_URL_ARG = "FILE_URL_ARG"
        const val FILE_NAME_ARG = "FILE_NAME_ARG"
        const val DIR_PATH_ARG = "DIR_PATH_ARG"
        const val DOWNLOAD_ID_ARG = "DOWNLOAD_ID_ARGUMENT"

        const val ACTION_ARG = "ACTION_ARG"
        const val ACTION_LOAD = "ACTION_LOAD"
        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"
        const val ACTION_REFRESH_STATE = "ACTION_REFRESH_STATE"

        const val BOOKS_UPDATE_BROADCAST_ACTION = "BOOKS_UPDATE_BROADCAST_ACTION"

        const val BOOKS_UPDATE_ACTION_LOADED_FILE_NAME = "BOOKS_UPDATE_ACTION_LOADED_FILE_NAME"
        const val BOOKS_UPDATE_ACTION_CANCELLED_LOADING = "BOOKS_UPDATE_ACTION_CANCELLED_LOADING"
        const val BOOKS_UPDATE_ACTION_START_LOADING_ID = "BOOKS_UPDATE_ACTION_START_LOADING_ID"
        const val BOOKS_UPDATE_ACTION_START_LOADING_FILENAME = "BOOKS_UPDATE_ACTION_START_LOADING_FILENAME"

    }
}
