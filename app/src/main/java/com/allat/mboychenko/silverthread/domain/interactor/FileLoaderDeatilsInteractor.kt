package com.allat.mboychenko.silverthread.domain.interactor

import com.allat.mboychenko.silverthread.data.storage.preferences.Storage

class FileLoaderDetailsInteractor(private val storage: Storage) : FileLoadingDetailsStorage {

    override fun getLoadingIds(): Map<String, Long> {
        return storage.getMap(DOWNLOADS_IDS_PREF_KEY)
    }

    override fun putLoadingId(url: String, id: Long) {
        val downloadIds = storage.getMap<String, Long>(DOWNLOADS_IDS_PREF_KEY).toMutableMap()
        downloadIds[url] = id
        storage.putMap(DOWNLOADS_IDS_PREF_KEY, downloadIds)
    }

    override fun removeIdFromLoadings(id: Long) {
        val loadings = getLoadingIds()
        val updatedEntry = loadings.filter { it.value != id}
        storage.putMap(DOWNLOADS_IDS_PREF_KEY, updatedEntry)
    }

    override fun cleanLoadingIds() {
        storage.remove(DOWNLOADS_IDS_PREF_KEY)
    }

    override fun putAllLoadingIds(map: Map<String, Long>) {
        val downloadIds = storage.getMap<String, Long>(DOWNLOADS_IDS_PREF_KEY).toMutableMap()
        downloadIds.putAll(map)
        storage.putMap(DOWNLOADS_IDS_PREF_KEY, downloadIds)
    }

    companion object {
        const val DOWNLOADS_IDS_PREF_KEY = "DOWNLOADS_IDS_PREF_KEY"
    }
}