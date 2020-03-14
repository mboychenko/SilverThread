package com.allat.mboychenko.silverthread.presentation.viewmodels

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.allat.mboychenko.silverthread.domain.interactor.DiaryNotesUseCase
import com.allat.mboychenko.silverthread.domain.models.DiaryNoteDomainModel
import com.allat.mboychenko.silverthread.presentation.intents.DiaryAction
import com.allat.mboychenko.silverthread.presentation.views.listitems.DiaryNoteItem
import com.allat.mboychenko.silverthread.presentation.viewstate.BaseEventCalendarViewState
import com.allat.mboychenko.silverthread.presentation.viewstate.DiaryViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DiaryNotesViewModel(private val diaryUseCase: DiaryNotesUseCase) : ViewModel(),
    IDiaryDialogEditor, IDiaryNotes, IDiaryNotesCalendarViewModel {

    private val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setPageSize(20)
        .build()

    private val action = MutableLiveData<DiaryAction>(DiaryAction.Load)

    private var diaryStateLatest: DiaryViewState? = null
    private val diaryViewState = action.switchMap {
        liveData(context = viewModelScope.coroutineContext) {

            val liveSource = when (it) {
                is DiaryAction.Load -> {
                    diaryStateLatest = DiaryViewState(loading = true)
                    emit(diaryStateLatest!!)
                    diaryUseCase.getAllDiaryNotesPaged()
                }
                is DiaryAction.Search -> {
                    val search = if (it.search.isEmpty()) null else it.search
                    diaryStateLatest = diaryStateLatest?.copy(searchState = search, loading = true)
                        ?: DiaryViewState(searchState = search, loading = true)
                    emit(diaryStateLatest!!)
                    getDiaryNotesPagedWithSelection(diaryStateLatest!!)
                }

                is DiaryAction.FilterDate -> {
                    diaryStateLatest = diaryStateLatest?.copy(filterDate = it.filterDate, loading = true)
                        ?: DiaryViewState(filterDate = it.filterDate, loading = true)
                    emit(diaryStateLatest!!)
                    getDiaryNotesPagedWithSelection(diaryStateLatest!!)
                }

                is DiaryAction.ClearDateFilter -> {
                    diaryStateLatest = diaryStateLatest?.copy(filterDate = null, loading = true)
                        ?: DiaryViewState(filterDate = null, loading = true)
                    emit(diaryStateLatest!!)
                    getDiaryNotesPagedWithSelection(diaryStateLatest!!)
                }

                else -> diaryUseCase.getAllDiaryNotesPaged()
            }

            val resultSource = liveSource.mapByPage(::mapToViewModel)

            val livePagedListBuilder = LivePagedListBuilder(resultSource, pagedListConfig).build()

            val result = livePagedListBuilder.map {
                diaryStateLatest = diaryStateLatest?.copy(data = it, loading = false)
                        ?: DiaryViewState(data = it, loading = false)
                diaryStateLatest!!
            }

            emitSource(result)

        }
    }

    private fun getDiaryNotesPagedWithSelection(viewState: DiaryViewState): DataSource.Factory<Int, DiaryNoteDomainModel> {
        return when {
            viewState.hasFilters() -> diaryUseCase.getDiaryNotesForComplexSelectionPaged(
                viewState.searchState,
                viewState.filterDate
            )
            else -> diaryUseCase.getAllDiaryNotesPaged()
        }
    }

    override fun bind(): LiveData<DiaryViewState> = diaryViewState

    override fun intent(action: DiaryAction) {
        this.action.value = action
    }

    private fun mapToViewModel(data: List<DiaryNoteDomainModel>): List<DiaryNoteItem> =
        data.map {
            DiaryNoteItem(
                it.id,
                it.note,
                it.start
            )
        }


    private val calendarAction = MutableLiveData<DiaryAction>(DiaryAction.Load)
    private val calendarModalState = calendarAction.switchMap {
        liveData(context = viewModelScope.coroutineContext) {
            emit(BaseEventCalendarViewState(null, loading = true))

            val dates = diaryUseCase.getNotesDays()
            val resultDates = processDates(dates)

            emit(BaseEventCalendarViewState(resultDates, false))
        }
    }

    private suspend fun processDates(events: List<Calendar>): List<Pair<Calendar, Int>> =
        withContext(viewModelScope.coroutineContext) {
            events
                .groupBy { it.get(Calendar.DAY_OF_YEAR) + it.get(Calendar.YEAR) * 366 }
                .values
                .map { it[0] to it.size }
        }

    override fun bindCalendar(): LiveData<BaseEventCalendarViewState> = calendarModalState

    override fun intentCalendar(action: DiaryAction) {
        if (action is DiaryAction.FilterDate) {
            this.action.value = action
        } else {
            calendarAction.value = action
        }
    }

    override fun addNote(start: Calendar, note: String) {
        diaryUseCase.addDiaryNote(DiaryNoteDomainModel(note, start = start))
    }

    override fun updateNote(id: String, start: Calendar, note: String) {
        diaryUseCase.updateDiaryNote(DiaryNoteDomainModel(note, id, start))
    }

    override fun removeNote(id: String) {
        diaryUseCase.removePracticeNote(DiaryNoteDomainModel(id = id))
    }

    override fun getNote(id: String): DiaryNoteItem? =
        diaryStateLatest?.data?.find { it.id == id }

}

interface IDiaryDialogEditor {
    fun addNote(
        start: Calendar,
        note: String
    )

    fun updateNote(
        id: String,
        start: Calendar,
        note: String
    )
    fun getNote(id: String): DiaryNoteItem?
}

interface IDiaryNotes {
    fun removeNote(id: String)
    fun intent(action: DiaryAction)
    fun bind(): LiveData<DiaryViewState>
}

interface IDiaryNotesCalendarViewModel {
    fun bindCalendar(): LiveData<BaseEventCalendarViewState>
    fun intentCalendar(action: DiaryAction)
}

