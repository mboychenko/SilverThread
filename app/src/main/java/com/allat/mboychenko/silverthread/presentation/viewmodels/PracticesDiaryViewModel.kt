package com.allat.mboychenko.silverthread.presentation.viewmodels

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import com.allat.mboychenko.silverthread.domain.interactor.DiaryPracticesUseCase
import com.allat.mboychenko.silverthread.domain.models.DiaryPracticeDomainModel
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeNoteItem
import java.util.*
import androidx.paging.PagedList
import com.allat.mboychenko.silverthread.presentation.intents.DiaryAction
import com.allat.mboychenko.silverthread.presentation.models.PracticesSort
import com.allat.mboychenko.silverthread.presentation.viewstate.PracticeCalendarViewState
import com.allat.mboychenko.silverthread.presentation.viewstate.PracticeViewState
import kotlinx.coroutines.*


class PracticesDiaryViewModel(private val diaryUseCase: DiaryPracticesUseCase) : ViewModel(),
    IPracticeNotesEditorVM, IPracticesDiaryCalendarViewModel {

    private val uniqPracticesNames: LiveData<List<String>> = diaryUseCase.getUniqPracticesNames()

    private val action = MutableLiveData<DiaryAction>(DiaryAction.Load)
    private val calendarAction = MutableLiveData<DiaryAction>(DiaryAction.Load)

    private val practicesViewState = object : MediatorLiveData<PracticeViewState>() {
        private var pagedSource: LiveData<PagedList<PracticeNoteItem>> = MutableLiveData()
        init {
            addSource(uniqPracticesNames) {
                value = value?.copy(uniqPracticeNames = it)
                    ?: PracticeViewState(uniqPracticeNames = it)
            }

            addSource(action) {
                val liveSource = when (it) {
                    is DiaryAction.Load -> {
                        value = PracticeViewState(loading = true)
                        diaryUseCase.getAllPracticesNotesPaged()
                    }

                    is DiaryAction.Search -> {
                        val search = if (it.search.isEmpty()) null else it.search
                        value = value?.copy(searchState = search, loading = true)
                            ?: PracticeViewState(searchState = search, loading = true)
                        getPracticesNotesPagedWithSelection(value!!)
                    }

                    is DiaryAction.FilterDatePractice -> {
                        value = value?.copy(
                            filterPractice = it.title,
                            filterDate = it.filterDate,
                            loading = true
                        )
                            ?: PracticeViewState(
                                filterPractice = it.title,
                                filterDate = it.filterDate,
                                loading = true
                            )
                        getPracticesNotesPagedWithSelection(value!!)
                    }

                    is DiaryAction.FilterPractice -> {
                        value = value?.copy(filterPractice = it.title, loading = true)
                                ?: PracticeViewState(filterPractice = it.title, loading = true)
                        getPracticesNotesPagedWithSelection(value!!)
                    }
                    is DiaryAction.Sort -> {
                        value = value?.copy(sortState = it.sort, loading = true)
                                ?: PracticeViewState(sortState = it.sort, loading = true)
                        getPracticesNotesPagedWithSelection(value!!)
                    }

                    is DiaryAction.ClearDateFilter -> {
                        value = value?.copy(filterDate = null, loading = true)
                                ?: PracticeViewState(filterDate = null, loading = true)
                        getPracticesNotesPagedWithSelection(value!!)
                    }

                    is DiaryAction.ClearFilterSortSelections -> {
                        value = value?.copy(
                            filterPractice = null,
                            sortState = PracticesSort.TIME,
                            loading = true
                        )
                            ?: PracticeViewState(
                                filterPractice = null,
                                sortState = PracticesSort.TIME,
                                loading = true
                            )
                        getPracticesNotesPagedWithSelection(value!!)
                    }

                    else -> diaryUseCase.getAllPracticesNotesPaged()
                }

                val resultSource = liveSource.mapByPage(::mapPracticesToNoteItems)

                LivePagedListBuilder(resultSource, pagedListConfig).build().also { source ->
                    removeSource(pagedSource)
                    pagedSource = source
                    addSource(source) { paged ->
                        value = value?.copy(data = paged, loading = false)
                            ?: PracticeViewState(data = paged, loading = false)
                    }
                }

            }
        }
    }

    private fun getPracticesNotesPagedWithSelection(model: PracticeViewState) =
        if (model.filtersInInitState()) {
            diaryUseCase.getAllPracticesNotesPaged()
        } else {
            diaryUseCase.getPracticesNotesPagedWithSelection(
                model.searchState,
                model.filterPractice,
                model.filterDate,
                model.sortState.name
            )
        }

    private var calendarStateLatest: PracticeCalendarViewState? = null
    private val calendarModalState = calendarAction.switchMap {
        liveData(context = viewModelScope.coroutineContext) {
            val dates = when (it) {
                is DiaryAction.Load -> {
                    calendarStateLatest = PracticeCalendarViewState(loading = true)
                    emit(calendarStateLatest!!)
                    diaryUseCase.getPracticesDays()
                }

                is DiaryAction.FilterPractice -> {
                    calendarStateLatest = calendarStateLatest?.copy(filterPractice = it.title, loading = true)
                        ?: PracticeCalendarViewState(filterPractice = it.title, loading = true)
                    emit(calendarStateLatest!!)
                    if (it.title != null) {
                        diaryUseCase.getPracticeDaysFor(it.title)
                    } else {
                        diaryUseCase.getPracticesDays()
                    }
                }

                else -> diaryUseCase.getPracticesDays()
            }

            val resultDates = processDates(dates)

            calendarStateLatest = calendarStateLatest?.copy(data = resultDates, uniqPracticeNames = uniqPracticesNames.value, loading = false)
                ?: PracticeCalendarViewState(resultDates, uniqPracticeNames = uniqPracticesNames.value, loading = false)
            emit(calendarStateLatest!!)
        }
    }

    private suspend fun processDates(events: List<Calendar>): List<Pair<Calendar, Int>> =
        withContext(Dispatchers.Default) {
            events
                .groupBy { it.get(Calendar.DAY_OF_YEAR) + it.get(Calendar.YEAR) * 366 }
                .values
                .map { it[0] to it.size }
        }

    private val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setPageSize(15)
        .build()

    override fun bind(): LiveData<PracticeViewState> = practicesViewState

    override fun intent(action: DiaryAction) {
        this.action.value = action
    }

    override fun bindUniqPracticesNames(): LiveData<List<String>> = uniqPracticesNames

    override fun bindCalendar(): LiveData<PracticeCalendarViewState> {
        return calendarModalState
    }

    override fun intentCalendar(action: DiaryAction) {
        if (action is DiaryAction.FilterDatePractice) {
            this.action.value = action
        } else {
            calendarAction.value = action
        }
    }

    private fun mapPracticesToNoteItems(data: List<DiaryPracticeDomainModel>?): List<PracticeNoteItem> =
        data?.map {
            PracticeNoteItem(
                it.id,
                it.title,
                it.notes,
                it.start,
                it.end,
                it.duration
            )
        } ?: emptyList()

    override fun findPracticeByGuid(guid: String): PracticeNoteItem? {
        return practicesViewState.value?.data?.find { it.noteId == guid }
    }

    override fun addPractice(title: String, start: Calendar, end: Calendar?, notes: String?) {
        diaryUseCase.addPracticeNote(
            DiaryPracticeDomainModel(
                title = title,
                start = start,
                end = end,
                duration = if (end != null) end.timeInMillis - start.timeInMillis else 0,
                notes = notes
            )
        )
    }

    override fun updatePractice(
        id: String,
        title: String,
        start: Calendar,
        end: Calendar?,
        notes: String?
    ) {
        diaryUseCase.updatePracticeNote(
            DiaryPracticeDomainModel(
                id,
                title,
                start,
                end,
                if (end != null) end.timeInMillis - start.timeInMillis else 0,
                notes = notes
            )
        )
    }

    override fun removePractice(id: String) {
        diaryUseCase.removePracticeNote(DiaryPracticeDomainModel(id))
    }

}

interface IPracticeNotesBaseViewModel {
    fun bind(): LiveData<PracticeViewState>
    fun intent(action: DiaryAction)
    fun removePractice(id: String)
}

interface IPracticeNotesEditorVM : IPracticeNotesBaseViewModel {
    fun addPractice(title: String,
                    start: Calendar = Calendar.getInstance(),
                    end: Calendar? = null,
                    notes: String? = null)
    fun updatePractice(id: String,
                       title: String,
                       start: Calendar = Calendar.getInstance(),
                       end: Calendar? = null,
                       notes: String? = null)
    fun findPracticeByGuid(guid: String): PracticeNoteItem?

    fun bindUniqPracticesNames(): LiveData<List<String>>
}

interface IPracticesDiaryCalendarViewModel {
    fun bindCalendar(): LiveData<PracticeCalendarViewState>
    fun intentCalendar(action: DiaryAction)
}





