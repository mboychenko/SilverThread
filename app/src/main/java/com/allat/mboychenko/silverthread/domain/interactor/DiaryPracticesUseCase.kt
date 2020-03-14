package com.allat.mboychenko.silverthread.domain.interactor

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.allat.mboychenko.silverthread.data.repositories.DiaryPracticesRepository
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryPracticesData
import com.allat.mboychenko.silverthread.domain.models.DiaryPracticeDomainModel
import kotlinx.coroutines.*
import java.util.*


class DiaryPracticesUseCase(private val diaryPracticesRepository: DiaryPracticesRepository) {

    private val useCaseScope = CoroutineScope(Dispatchers.Default)

    fun getAllPracticesNotesPaged(): DataSource.Factory<Int, DiaryPracticeDomainModel> =
            diaryPracticesRepository.getAllPracticesNotesPaged()
            .mapByPage(::mapDataToPracticeViewModelList)

    fun getPracticesNotesPagedWithSelection(
        search: String? = null,
        nameFilter: String? = null,
        dayFilter: Pair<Calendar, Calendar?>? = null,
        sort: String? = null
    ): DataSource.Factory<Int, DiaryPracticeDomainModel> =
        diaryPracticesRepository.getPracticesPagedSelection(
            search = search,
            nameFilter = nameFilter,
            dayFilter = dayFilter,
            sort = sort
        ).mapByPage(::mapDataToPracticeViewModelList)

    fun getUniqPracticesNames(): LiveData<List<String>> = diaryPracticesRepository.getUniqPracticesNames()

    suspend fun getPracticesDays(): List<Calendar> =
        withContext(useCaseScope.coroutineContext) {
            diaryPracticesRepository.getPracticesDays()
        }

    suspend fun getPracticeDaysFor(practice: String): List<Calendar> =
        withContext(useCaseScope.coroutineContext) {
            diaryPracticesRepository.getPracticeDaysFor(practice)
        }

    fun addPracticeNote(note: DiaryPracticeDomainModel) {
        useCaseScope.launch {
            diaryPracticesRepository.insert(note.mapModelToData())
        }
    }

    fun updatePracticeNote(note: DiaryPracticeDomainModel) {
        useCaseScope.launch {
            diaryPracticesRepository.update(note.mapModelToData())
        }
    }

    fun removePracticeNote(note: DiaryPracticeDomainModel) {
        useCaseScope.launch {
            diaryPracticesRepository.delete(note.mapModelToData())
        }
    }

    @SuppressLint("DefaultLocale")
    private fun DiaryPracticeDomainModel.mapModelToData() =
        DiaryPracticesData(id, title.toLowerCase(), start, end, duration, notes)

    private fun mapDataToPracticeViewModelList(diaryDataModels: List<DiaryPracticesData>): List<DiaryPracticeDomainModel> {
        val result = mutableListOf<DiaryPracticeDomainModel>()
        diaryDataModels.forEach { result.add(mapDataToPracticeViewModel(it)) }
        return result
    }

    private fun mapDataToPracticeViewModel(model: DiaryPracticesData) =
        DiaryPracticeDomainModel(
            model.id,
            model.title,
            model.start,
            model.end,
            model.duration,
            model.notes
        )


}