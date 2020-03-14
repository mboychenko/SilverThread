package com.allat.mboychenko.silverthread.data.storage.db.diary

import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.allat.mboychenko.silverthread.data.storage.db.ALL
import com.allat.mboychenko.silverthread.data.storage.db.AND
import com.allat.mboychenko.silverthread.data.storage.db.BETWEEN
import com.allat.mboychenko.silverthread.data.storage.db.DESC
import com.allat.mboychenko.silverthread.data.storage.db.FROM
import com.allat.mboychenko.silverthread.data.storage.db.LIKE
import com.allat.mboychenko.silverthread.data.storage.db.ORDER_BY
import com.allat.mboychenko.silverthread.data.storage.db.SELECT
import com.allat.mboychenko.silverthread.data.storage.db.WHERE
import com.allat.mboychenko.silverthread.data.storage.db.WHITESPACE
import com.allat.mboychenko.silverthread.presentation.helpers.applyDayEnd
import com.allat.mboychenko.silverthread.presentation.helpers.applyDayStart
import java.lang.StringBuilder
import java.util.*

@Dao
interface DiaryNotesDao {

    @Query("SELECT * FROM diary_notes_table ORDER BY start DESC")
    fun selectAllDiaryNotesPaged(): DataSource.Factory<Int, DiaryNotesData>

    @RawQuery(observedEntities = [DiaryNotesData::class])
    fun selectDiaryNotesComplexRawPaged(query: SupportSQLiteQuery): DataSource.Factory<Int, DiaryNotesData>

    @Query("SELECT * FROM diary_notes_table")
    fun selectDiaryNotes(): List<DiaryNotesData>

    @Query("SELECT start FROM diary_notes_table ORDER BY start DESC")
    fun getNotesDays(): List<Calendar>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: DiaryNotesData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notes: List<DiaryNotesData>)

    @Delete
    suspend fun delete(note: DiaryNotesData)

    @Update
    suspend fun update(note: DiaryNotesData)
}

internal fun buildDiaryNoteSelectionQuery(
    search: String? = null,
    dateFilter: Pair<Calendar, Calendar?>? = null
): SupportSQLiteQuery {
    val queryBuilder = StringBuilder()

    queryBuilder.append(
        SELECT, WHITESPACE, ALL, FROM,
        DiaryFieldsContract.DIARY_NOTES_TABLE_NAME
    )

    dateFilter?.let {
        val (first, last) = it
        val start = first.applyDayStart()
        val end = (last ?: first.clone() as Calendar).applyDayEnd()

        queryBuilder.append(
            WHERE,
            DiaryFieldsContract.START,
            BETWEEN,
            start.timeInMillis,
            AND,
            end.timeInMillis
        )
    }

    search?.let {
        if (dateFilter != null) {
            queryBuilder.append(AND, DiaryFieldsContract.NOTES, LIKE, "'%$it%'")
        } else {
            queryBuilder.append(WHERE, DiaryFieldsContract.NOTES, LIKE, "'%$it%'")
        }
    }

    queryBuilder.append(ORDER_BY, DiaryFieldsContract.START, DESC)

    return SimpleSQLiteQuery(queryBuilder.toString())
}
