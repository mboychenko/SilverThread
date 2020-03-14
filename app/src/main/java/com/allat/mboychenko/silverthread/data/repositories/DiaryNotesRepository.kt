package com.allat.mboychenko.silverthread.data.repositories

import androidx.paging.DataSource
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryNotesDao
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryNotesData
import com.allat.mboychenko.silverthread.data.storage.db.diary.buildDiaryNoteSelectionQuery
import java.util.*

class DiaryNotesRepository(private val diaryDao: DiaryNotesDao) {

    fun getAllDiaryNotesPaged(): DataSource.Factory<Int, DiaryNotesData> =
        diaryDao.selectAllDiaryNotesPaged()

    fun getDiaryNotesForComplexSelectionPaged(
        search: String? = null,
        dateFilter: Pair<Calendar, Calendar?>? = null
    ) : DataSource.Factory<Int, DiaryNotesData> =
        diaryDao.selectDiaryNotesComplexRawPaged(buildDiaryNoteSelectionQuery(search, dateFilter))

    fun getNotesDays(): List<Calendar> = diaryDao.getNotesDays()

    fun getDiaryNotes(): List<DiaryNotesData> = diaryDao.selectDiaryNotes()

    suspend fun insertAll(notes: List<DiaryNotesData>) {
        diaryDao.insertAll(notes)
    }

    suspend fun insert(note: DiaryNotesData) {
        diaryDao.insert(note)
    }

    suspend fun delete(note: DiaryNotesData) {
        diaryDao.delete(note)
    }

    suspend fun update(note: DiaryNotesData) {
        diaryDao.update(note)
    }

}