package com.allat.mboychenko.silverthread.presentation.presenters

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.allat.mboychenko.silverthread.presentation.views.fragments.IDownloadsFragmentView
import android.widget.Toast
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.views.listitems.LoadedFileItem


class DownloadsPresenter(val context: Context) : BasePresenter<IDownloadsFragmentView>() {

    override fun attachView(view: IDownloadsFragmentView) {
        super.attachView(view)
        context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        checkFilesList()
    }

    override fun detachView() {
        super.detachView()
        context.unregisterReceiver(onDownloadComplete)
    }

    private fun checkFilesList() {
        if (isExternalStorageReadable()) {
            manageAddToSubscription(
                runTaskOnBackgroundWithResult(ExecutorThread.IO,
                    {
                        val directory = getPublicDownloadsStorageDir(WEB_DOWNLOADS_FOLDER_NAME)
                        val files = directory?.listFiles().orEmpty().asList()
                        files.filter { !it.isDirectory }
                    },
                    {
                        if (it.isEmpty()) {
                            view?.noFilesInDirectory()
                        } else {
                            view?.filesList(
                                it.map { file ->
                                    LoadedFileItem(
                                        file.name,
                                        humanReadableByteCount(file.length()),
                                        file.path,
                                        view!!::removeLoadedItem
                                    )
                                }
                            )
                        }
                    }
                )
            )
        } else {
            Toast.makeText(context, R.string.cant_access_storage, Toast.LENGTH_LONG).show()
        }
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkFilesList()
        }
    }

}