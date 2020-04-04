package com.allat.mboychenko.silverthread.domain.models

import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryNotesData
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryPracticesData

data class DataBackupModel(
    val practices: List<DiaryPracticesData> = emptyList(),
    val notes: List<DiaryNotesData> = emptyList(),
    val favQuotes: Set<Int> = emptySet(),
    val favParables: Set<Int> = emptySet()
)