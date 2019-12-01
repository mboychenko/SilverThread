package com.allat.mboychenko.silverthread.domain.interactor

import android.content.Context
import com.allat.mboychenko.silverthread.data.storage.Storage
import com.allat.mboychenko.silverthread.data.storage.StorageImplementation

class PracticeStorageInteractor(private val storage: Storage) : PracticeStorage {

    constructor(context: Context) : this(StorageImplementation(context))

    override fun setStartOffsetSeconds(seconds: Int) {
        storage.putInt(PRACTICE_OFFSET_PREF_KEY, seconds)
    }

    override fun getStartOffsetSeconds(): Int =
        storage.getInt(PRACTICE_OFFSET_PREF_KEY)

    companion object {
        private const val PRACTICE_OFFSET_PREF_KEY = "PRACTICE_OFFSET_PREF_KEY"
    }

}