package com.allat.mboychenko.silverthread.data.storage.db.diary

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.allat.mboychenko.silverthread.data.storage.db.ALL
import com.allat.mboychenko.silverthread.data.storage.db.AND
import com.allat.mboychenko.silverthread.data.storage.db.BETWEEN
import com.allat.mboychenko.silverthread.data.storage.db.COLLATE_NOCASE
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
interface PracticesDiaryDao {

    @Query("SELECT * FROM diary_practices_table ORDER BY start DESC")
    fun selectAllDiaryPracticesPaged(): DataSource.Factory<Int, DiaryPracticesData>

    @Query("SELECT DISTINCT lower(title) FROM diary_practices_table ORDER BY title ASC")
    fun selectUniqPracticesLive(): LiveData<List<String>>

    @RawQuery(observedEntities = [DiaryPracticesData::class])
    fun selectDiaryPracticesRaw(query: SupportSQLiteQuery): DataSource.Factory<Int, DiaryPracticesData>

    @Query("SELECT start FROM diary_practices_table ORDER BY start DESC")
    fun selectPracticesDays(): List<Calendar>

    @Query("SELECT start FROM diary_practices_table WHERE title = :practice ORDER BY start DESC")
    fun selectPracticeDaysFor(practice: String): List<Calendar>

    @Query("SELECT * FROM diary_practices_table")
    fun selectAllPracticesNotes(): List<DiaryPracticesData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: DiaryPracticesData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notes: List<DiaryPracticesData>)

    @Delete
    suspend fun delete(note: DiaryPracticesData)

    @Update
    suspend fun update(note: DiaryPracticesData)

}

internal fun buildDiaryPracticesSelectionQuery(
    sortOptions: String? = null,
    filterTitle: String? = null,
    filterDay: Pair<Calendar, Calendar?>? = null,
    search: String? = null
): SupportSQLiteQuery {
    val queryBuilder = StringBuilder()

    queryBuilder.append(
        SELECT, WHITESPACE, ALL, FROM,
        DiaryFieldsContract.DIARY_PRACTICE_TABLE_NAME
    )

    filterDay?.let {
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

    filterTitle?.let {
        if (filterDay != null) {
            queryBuilder.append(AND, DiaryFieldsContract.TITLE, LIKE, "'$it'", COLLATE_NOCASE)
        } else {
            queryBuilder.append(WHERE, DiaryFieldsContract.TITLE, LIKE, "'$it'", COLLATE_NOCASE)

        }
    }

    search?.let {
        if (filterTitle != null || filterDay != null) {
            queryBuilder.append(AND, DiaryFieldsContract.NOTES, LIKE, "'%$it%'")
        } else {
            queryBuilder.append(WHERE, DiaryFieldsContract.NOTES, LIKE, "'%$it%'")
        }
    }

    sortOptions?.let {
        queryBuilder.append(ORDER_BY, sortOptions)
    } ?: run {
        queryBuilder.append(ORDER_BY, DiaryFieldsContract.START, DESC)
    }

    return SimpleSQLiteQuery(queryBuilder.toString())
}