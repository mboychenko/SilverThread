package com.allat.mboychenko.silverthread.domain.interactor

interface BooksLoaderDetailsStorage {

    fun requestPermissionSaveData(key: String, value: String)
    fun requestPermissionRestoreData(key: String): String?
    fun requestPermissionRemoveData(key: String)

    fun getLastBookPage(name: String): Int
    fun saveLastBookPage(name: String, page: Int)
    fun removeLastBookPage(name: String)
}