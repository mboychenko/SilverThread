package com.allat.mboychenko.silverthread.domain.interactor

import android.content.Context
import com.allat.mboychenko.silverthread.data.storage.Storage
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation

class ChetverikStorageInteractor(private val storage: Storage) : ChetverikStorage {

    constructor(context: Context) : this(StorageImplementation(context))

    override fun setStartOffsetSeconds(seconds: Int) {
        storage.putInt(CHETVERIK_OFFSET_PREF_KEY, seconds)
    }

    override fun getStartOffsetSeconds(): Int =
        storage.getInt(CHETVERIK_OFFSET_PREF_KEY)

    companion object {
        private const val CHETVERIK_OFFSET_PREF_KEY = "CHETVERIK_OFFSET_PREF_KEY"
    }

}