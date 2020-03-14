package com.allat.mboychenko.silverthread.data.repositories

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.allat.mboychenko.silverthread.data.storage.db.ASC
import com.allat.mboychenko.silverthread.data.storage.db.DESC
import com.allat.mboychenko.silverthread.data.storage.db.diary.*
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryFieldsContract.DURATION
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryFieldsContract.START
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryFieldsContract.TITLE
import java.util.*

class DiaryPracticesRepository(private val diaryDao: PracticesDiaryDao) {

    fun getPracticesPagedSelection(
        search: String? = null,
        nameFilter: String? = null,
        dayFilter: Pair<Calendar, Calendar?>? = null,
        sort: String? = null
    ): DataSource.Factory<Int, DiaryPracticesData> {

        val sortOption = when(sort) {
            FIELD_DURATION -> "$DURATION$DESC"
            FIELD_NAME -> "$TITLE$ASC, $START$DESC"
            else -> "$START$DESC"
        }

        return with(diaryDao) {
            selectDiaryPracticesRaw(buildDiaryPracticesSelectionQuery(
                sortOptions = sortOption,
                filterTitle = nameFilter,
                filterDay = dayFilter,
                search = search
            ))
        }
    }

    fun getAllPracticesNotesPaged(): DataSource.Factory<Int, DiaryPracticesData> =
        diaryDao.selectAllDiaryPracticesPaged()

    fun getUniqPracticesNames(): LiveData<List<String>> = diaryDao.selectUniqPracticesLive()

    fun getPracticesDays(): List<Calendar> = diaryDao.selectPracticesDays()

    fun getPracticeDaysFor(practice: String): List<Calendar> = diaryDao.selectPracticeDaysFor(practice)

    fun getPractices(): List<DiaryPracticesData> = diaryDao.selectAllPracticesNotes()

    suspend fun insert(note: DiaryPracticesData) {
        diaryDao.insert(note)
    }

    suspend fun insertAll(notes: List<DiaryPracticesData>) {
        diaryDao.insertAll(notes)
    }

    suspend fun delete(note: DiaryPracticesData) {
        diaryDao.delete(note)
    }

    suspend fun update(note: DiaryPracticesData) {
        diaryDao.update(note)
    }

    companion object {
        private const val FIELD_DURATION = "DURATION"
        private const val FIELD_NAME = "NAME"
    }

}