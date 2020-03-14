package com.allat.mboychenko.silverthread.domain.interactor

import androidx.paging.DataSource
import com.allat.mboychenko.silverthread.data.repositories.DiaryNotesRepository
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryNotesData
import com.allat.mboychenko.silverthread.domain.models.DiaryNoteDomainModel
import kotlinx.coroutines.*
import java.util.*

class DiaryNotesUseCase(private val diaryNotesRepository: DiaryNotesRepository) {

    private val useCaseScope = CoroutineScope(Dispatchers.Default)

    fun getAllDiaryNotesPaged(): DataSource.Factory<Int, DiaryNoteDomainModel> =
        diaryNotesRepository.getAllDiaryNotesPaged().mapByPage(::createDiaryNotesViewModelList)

    fun getDiaryNotesForComplexSelectionPaged(
        search: String? = null,
        dateFilter: Pair<Calendar, Calendar?>? = null
    ): DataSource.Factory<Int, DiaryNoteDomainModel> =
        diaryNotesRepository.getDiaryNotesForComplexSelectionPaged(search, dateFilter)
            .mapByPage(::createDiaryNotesViewModelList)

    suspend fun getNotesDays(): List<Calendar> =
        withContext(useCaseScope.coroutineContext) {
            diaryNotesRepository.getNotesDays()
        }

    fun addDiaryNote(note: DiaryNoteDomainModel) {
        useCaseScope.launch {
            diaryNotesRepository.insert(note.mapToData())
        }
    }

    fun removePracticeNote(note: DiaryNoteDomainModel) {
        useCaseScope.launch {
            diaryNotesRepository.delete(note.mapToData())
        }
    }

    fun updateDiaryNote(note: DiaryNoteDomainModel) {
        useCaseScope.launch {
            diaryNotesRepository.update(note.mapToData())
        }
    }

    private fun DiaryNoteDomainModel.mapToData() = DiaryNotesData(id, note, start)

    private fun createDiaryNotesViewModelList(diaryDataModels: List<DiaryNotesData>): List<DiaryNoteDomainModel> {
        val result = mutableListOf<DiaryNoteDomainModel>()

        diaryDataModels.forEach {
            result.add(
                DiaryNoteDomainModel(
                    it.note,
                    it.id,
                    it.start
                )
            )
        }

        return result
    }
}