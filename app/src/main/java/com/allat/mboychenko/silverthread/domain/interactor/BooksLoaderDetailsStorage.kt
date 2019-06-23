package com.allat.mboychenko.silverthread.domain.interactor

interface BooksLoaderDetailsStorage {

    fun getBooksLoadingIds(): Map<String, Long>
    fun putBookLoadingId(url: String, id: Long)
    fun removeIdFromBookLoadings(id: Long)
    fun cleanLoadingIds()

    fun requestPermissionSaveData(key: String, value: String)
    fun requestPermissionRestoreData(key: String): String?
    fun requestPermissionRemoveData(key: String)

    fun getLastBookPage(name: String): Int
    fun saveLastBookPage(name: String, page: Int)
    fun removeLastBookPage(name: String)
}