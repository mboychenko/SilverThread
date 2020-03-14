package com.allat.mboychenko.silverthread.domain.interactor

import android.content.Context
import com.allat.mboychenko.silverthread.data.storage.preferences.Storage
import com.allat.mboychenko.silverthread.data.storage.preferences.StorageImplementation

class PracticeStorageInteractor(private val storage: Storage) : PracticeStorage {

    constructor(context: Context) : this(
        StorageImplementation(
            context
        )
    )

    override fun setStartOffsetSeconds(seconds: Int) {
        storage.putInt(PRACTICE_OFFSET_PREF_KEY, seconds)
    }

    override fun getStartOffsetSeconds(): Int =
        storage.getInt(PRACTICE_OFFSET_PREF_KEY)

    override fun setAllatLengthStateShort(short: Boolean) {
        storage.putBoolean(PRACTICE_ALLAT_LEN_PREF_KEY, short)
    }

    override fun getAllatLengthStateShort() =
        storage.getBoolean(PRACTICE_ALLAT_LEN_PREF_KEY, false)

    companion object {
        private const val PRACTICE_OFFSET_PREF_KEY = "PRACTICE_OFFSET_PREF_KEY"
        private const val PRACTICE_ALLAT_LEN_PREF_KEY = "PRACTICE_ALLAT_LEN_PREF_KEY"
    }

}