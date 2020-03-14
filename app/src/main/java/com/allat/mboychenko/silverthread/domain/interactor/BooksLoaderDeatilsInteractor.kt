package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.preferences.Storage

class BooksLoaderDeatilsInteractor(private val storage: Storage): BooksLoaderDetailsStorage {

    override fun requestPermissionSaveData(key: String, value: String) {
        storage.putString(key, value)
    }

    override fun requestPermissionRestoreData(key: String): String? = storage.getString(key)

    override fun requestPermissionRemoveData(key: String) {
        storage.remove(key)
    }

    override fun saveLastBookPage(name: String, page: Int) {
        storage.putInt(String.format(BOOK_CURRENT_PAGE, name), page)
    }

    override fun getLastBookPage(name: String) =
        storage.getInt(String.format(BOOK_CURRENT_PAGE, name))

    override fun removeLastBookPage(name: String) {
        storage.remove(String.format(BOOK_CURRENT_PAGE, name))
    }

    companion object {
        const val BOOK_CURRENT_PAGE = "BOOK_CURRENT_PAGE_%s"
    }
}