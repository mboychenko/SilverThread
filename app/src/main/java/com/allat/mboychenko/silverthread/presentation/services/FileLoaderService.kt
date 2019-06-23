package com.allat.mboychenko.silverthread.presentation.services

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import com.allat.mboychenko.silverthread.domain.helper.BooksHelper
import org.koin.android.ext.android.inject
import java.io.File

class FileLoaderService : JobIntentService() {

    private val storage: BooksLoaderDetailsStorage by inject()
    private val booksHelper: BooksHelper by inject()
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val downloadManager by lazy { getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    override fun onHandleWork(intent: Intent) {
        val url = intent.getStringExtra(FILE_URL_ARG)
        val fileName = intent.getStringExtra(FILE_NAME_ARG)
        val path = intent.getStringExtra(DIR_PATH_ARG)
        val action = intent.getStringExtra(ACTION_ARG)
        val cancelId = intent.getLongExtra(DOWNLOAD_ID_ARG, -1)
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)

        when {
            action?.equals(ACTION_CANCEL_DOWNLOAD) == true -> {
                if (cancelId != -1L) {
                    val loadingIds = storage.getBooksLoadingIds()
                    downloadManager.remove(cancelId)
                    loadingIds.entries.find { it.value == cancelId }?.let {
                        finishLoading(it.key, cancelId)
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
                val inProgress = urlInProgressId(it)
                if (!inProgress && !path.isNullOrEmpty() && !fileName.isNullOrEmpty()) {
                    loadFile(it, path, fileName)
                }
            }
        }
    }

    private fun urlInProgressId(url: String): Boolean {
        return storage.getBooksLoadingIds()[url] != null
    }


    private fun updateLoadingStatus(url: String, id: Long) {
        val query = DownloadManager.Query().apply {
            setFilterById(id)
        }
        val c = downloadManager.query(query)
        if(c.moveToFirst()) {
            when(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING -> {
                    //continue
                }

                DownloadManager.STATUS_SUCCESSFUL,
                DownloadManager.STATUS_FAILED -> {
                    finishLoading(url, id)
                }
            }
        }
    }

    private fun finishLoading(url: String, id: Long) {
        storage.removeIdFromBookLoadings(id)
        val fileName = booksHelper.getBookByUrl(url).fileName
        localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
            .apply { putExtra(BOOKS_UPDATE_ACTION_CANCELLED_LOADING, fileName) })
    }

    private fun loadFile(url: String, dirPath: String, fileName: String) {
        val destinationFileUri = Uri.fromFile(File(dirPath, fileName))
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(destinationFileUri)
            .setAllowedOverMetered(true)

        val downloadID = downloadManager.enqueue(request)

        storage.putBookLoadingId(url, downloadID)
        localBroadcastManager.sendBroadcast(Intent(BOOKS_UPDATE_BROADCAST_ACTION)
            .apply {
                putExtra(BOOKS_UPDATE_ACTION_START_LOADING_ID, downloadID)
                putExtra(BOOKS_UPDATE_ACTION_START_LOADING_FILENAME, fileName)
            })
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

        const val BOOKS_UPDATE_ACTION_CANCELLED_LOADING = "BOOKS_UPDATE_ACTION_CANCELLED_LOADING"
        const val BOOKS_UPDATE_ACTION_START_LOADING_ID = "BOOKS_UPDATE_ACTION_START_LOADING_ID"
        const val BOOKS_UPDATE_ACTION_START_LOADING_FILENAME = "BOOKS_UPDATE_ACTION_START_LOADING_FILENAME"

    }
}
