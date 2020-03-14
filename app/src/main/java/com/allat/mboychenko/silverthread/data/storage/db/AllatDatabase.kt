package com.allat.mboychenko.silverthread.data.storage.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.allat.mboychenko.silverthread.data.storage.db.diary.*

@Database(entities = [DiaryPracticesData::class, DiaryNotesData::class], version = 1, exportSchema = false)
@TypeConverters(CalendarTypeConverter::class)
abstract class AllatDatabase : RoomDatabase() {

    abstract fun diaryNotesDao(): DiaryNotesDao
    abstract fun practicesDiaryDao(): PracticesDiaryDao

    companion object {
        @Volatile
        private var INSTANCE: AllatDatabase? = null

        fun getDatabase(context: Context): AllatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AllatDatabase::class.java,
                    DATABASE_NAME
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private const val DATABASE_NAME = "allat_database"
    }
}