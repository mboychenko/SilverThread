package com.allat.mboychenko.silverthread.data.storage.db.diary

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.NOCASE
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryFieldsContract.DIARY_PRACTICE_TABLE_NAME
import java.util.*

@Entity(tableName = DIARY_PRACTICE_TABLE_NAME)
class DiaryPracticesData(
    @ColumnInfo(name = DiaryFieldsContract.ID) @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = DiaryFieldsContract.TITLE, collate = NOCASE) val title: String,
    @ColumnInfo(name = DiaryFieldsContract.START) val start: Calendar = Calendar.getInstance(),
    @ColumnInfo(name = DiaryFieldsContract.END) val end: Calendar? = null,
    @ColumnInfo(name = DiaryFieldsContract.DURATION) val duration: Long = 0,
    @ColumnInfo(name = DiaryFieldsContract.NOTES) val notes: String? = null
)
