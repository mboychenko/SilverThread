package com.allat.mboychenko.silverthread.data.storage.db.diary

import androidx.room.*
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryFieldsContract.DIARY_NOTES_TABLE_NAME
import java.util.*

@Entity(tableName = DIARY_NOTES_TABLE_NAME)
data class DiaryNotesData(
    @ColumnInfo(name = DiaryFieldsContract.ID) @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = DiaryFieldsContract.NOTES) val note: String,
    @ColumnInfo(name = DiaryFieldsContract.START) val start: Calendar = Calendar.getInstance()
)
