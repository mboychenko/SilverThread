package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.net.Uri
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.views.listitems.BookItem

interface IBooksFragmentView {
    fun notifyBooksUpdate()
    fun updateItems(items: List<BookItem>)
    fun bookRemoved(book: BookItem)
    fun bookLoaded(fileName: String)
    fun loadingStarted(fileName: String, loadingId: Int)
    fun bookLoadingCancelled(fileName: String)
    fun requestStoragePermission()
    fun shareBookLink(bookTitle: String, bookUrl: String)
    fun openBook(bookUri: Uri)
}