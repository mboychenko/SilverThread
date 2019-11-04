package com.allat.mboychenko.silverthread.presentation.views.fragments

import com.allat.mboychenko.silverthread.presentation.views.listitems.LoadedFileItem

interface IDownloadsFragmentView {
    fun filesList(files: List<LoadedFileItem>)
    fun noFilesDescriptionVisibility(visible: Boolean = true)
    fun showNoPermissions()
    fun removeLoadedItem(item: LoadedFileItem)
    fun requestStoragePermission()
}