package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.Storage

class BooksLoaderDeatilsInteractor(val storage: Storage): BooksLoaderDetailsStorage {

    /**
     * todo change it to put in IO thread
     */
    override fun getBooksLoadingIds(): Map<String, Int> {
        return storage.getMap(BOOKS_DOWNLOADS_IDS_PREF_KEY)
    }

    /**
     * todo change it to put in IO thread
     */
    override fun putBookLoadingId(url: String, id: Int) {
        val booksDownloadIds = storage.getMap<String, Int>(BOOKS_DOWNLOADS_IDS_PREF_KEY).toMutableMap()
        booksDownloadIds[url] = id
        storage.putMap(BOOKS_DOWNLOADS_IDS_PREF_KEY, booksDownloadIds)
    }

    /**
     * todo change it to put in IO thread
     */
    override fun removeIdFromBookLoadings(id: Int) {
        val loadings = getBooksLoadingIds()
        val updatedEntry = loadings.filter { it.value != id}
        storage.putMap(BOOKS_DOWNLOADS_IDS_PREF_KEY, updatedEntry)
    }

    override fun cleanLoadingIds() {
        storage.remove(BOOKS_DOWNLOADS_IDS_PREF_KEY)
    }

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
        const val BOOKS_DOWNLOADS_IDS_PREF_KEY = "BOOKS_DOWNLOADS_IDS_PREF_KEY"
        const val BOOK_CURRENT_PAGE = "BOOK_CURRENT_PAGE_%s"
    }
}