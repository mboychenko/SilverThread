package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.allat.mboychenko.silverthread.presentation.views.fragments.IDownloadsFragmentView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.allat.mboychenko.silverthread.presentation.helpers.*
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService
import com.allat.mboychenko.silverthread.presentation.services.FileLoaderService.Companion.DOWNLOADING_EXT
import com.allat.mboychenko.silverthread.presentation.views.listitems.LoadedFileItem


class DownloadsPresenter(val context: Context) : BasePresenter<IDownloadsFragmentView>() {

    override fun attachView(view: IDownloadsFragmentView) {
        super.attachView(view)
        LocalBroadcastManager.getInstance(context).registerReceiver(
            onDownloadComplete,
            IntentFilter(FileLoaderService.FILES_STATUS_UPDATED_BROADCAST_ACTION)
        )
        checkFilesList()
    }

    fun checkPermission() {
        if (isExternalStorageAvailable().not() || extStoragePermissionGranted(context).not()) {
            view?.requestStoragePermission()
        }
    }

    override fun detachView() {
        super.detachView()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onDownloadComplete)
    }

    fun checkFilesList() {
        if (isExternalStorageAvailable() && extStoragePermissionGranted(context)) {
            manageAddToSubscription(
                runTaskOnBackgroundWithResult(ExecutorThread.IO,
                    {
                        val directory = getPublicDownloadsStorageDir(WEB_DOWNLOADS_FOLDER_NAME)
                        val files = directory?.listFiles().orEmpty().asList()
                        files.filter { !it.isDirectory && !it.path.endsWith(DOWNLOADING_EXT) }
                    },
                    {
                        FileLoaderService.commandRefreshLoadings(context.applicationContext)
                        if (it.isEmpty()) {
                            view?.noFilesDescriptionVisibility(true)
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
            view?.showNoPermissions()
        }
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkFilesList()
        }
    }

}