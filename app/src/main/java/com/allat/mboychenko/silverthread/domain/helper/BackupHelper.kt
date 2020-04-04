package com.allat.mboychenko.silverthread.domain.helper

import com.allat.mboychenko.silverthread.data.repositories.DiaryNotesRepository
import com.allat.mboychenko.silverthread.data.repositories.DiaryPracticesRepository
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryNotesData
import com.allat.mboychenko.silverthread.data.storage.db.diary.DiaryPracticesData
import com.allat.mboychenko.silverthread.domain.interactor.ParablesDetailsStorage
import com.allat.mboychenko.silverthread.domain.interactor.QuotesDetailsStorage
import com.allat.mboychenko.silverthread.domain.models.DataBackupModel
import com.allat.mboychenko.silverthread.presentation.helpers.getBackupFile
import com.google.gson.*
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.text.Charsets.UTF_8

/**
 * Real implementation hidden
 */
class BackupHelper(
    private val diaryNotesRepo: DiaryNotesRepository,
    private val diaryPracticeRepo: DiaryPracticesRepository,
    private val quoteStorage: QuotesDetailsStorage,
    private val parablesStorage: ParablesDetailsStorage
) {
    private val coroutineScp = CoroutineScope(Dispatchers.Default)

    suspend fun backup(code: String): Status {
        return withContext(coroutineScp.coroutineContext) {
            getBackupFile()?.let { file ->
                val favoriteQuotes = quoteStorage.getFavoriteQuotesPositions()
                val favoriteParables = parablesStorage.getFavoriteParablesPositions()
                val practices = diaryPracticeRepo.getPractices()
                val notes = diaryNotesRepo.getDiaryNotes()

                val backupData = prepareBackup(practices, notes, favoriteQuotes, favoriteParables, code)

                file.writeBytes(backupData)
                Status.SUCCESS
            } ?: run {
                Status.NO_ACCESS_TO_FILE
            }
        }
    }

    suspend fun restore(code: String): Status {
        return withContext(coroutineScp.coroutineContext) {
            getBackupFile()?.let {
                if (!it.exists()) {
                    return@withContext Status.NO_BACKUP_FILE
                }

                try {
                    val backupModel = restoreBackupFromFile(it, code)
                    parablesStorage.restoreFavoriteParables(backupModel.favParables)
                    quoteStorage.restoreFavoriteQuotes(backupModel.favQuotes)
                    diaryPracticeRepo.insertAll(backupModel.practices)
                    diaryNotesRepo.insertAll(backupModel.notes)
                    Status.SUCCESS
                } catch (e: Exception) {
                    Status.WRONG_PASSWORD
                }
            } ?: run {
                Status.NO_BACKUP_FILE
            }
        }
    }

    private fun prepareBackup(
        practices: List<DiaryPracticesData>,
        notes: List<DiaryNotesData>,
        favoriteQuotes: Set<Int>,
        favoriteParables: Set<Int>,
        code: String
    ): ByteArray {
        val backupModel = DataBackupModel(practices, notes, favoriteQuotes, favoriteParables)
        val backup = getGsonProcessor().toJson(backupModel)
        return backup.toByteArray()
    }

    private fun restoreBackupFromFile(backupFile: File, code: String): DataBackupModel {
        val fileContents = backupFile.readBytes()

        return getGsonProcessor().fromJson(fileContents.toString(UTF_8), DataBackupModel::class.java)
    }

    private fun getGsonProcessor(): Gson {
        val jsonCalendarSer: JsonSerializer<Calendar> =
            JsonSerializer { src, _, _ -> if (src == null) null else JsonPrimitive(src.timeInMillis) }

        val jsonCalendarDeser: JsonDeserializer<Calendar> =
            JsonDeserializer<Calendar> { json, _, _ ->
                if (json == null) null else Calendar.getInstance().apply {
                    timeInMillis = json.asLong
                }
            }

        return GsonBuilder()
            .registerTypeAdapter(Calendar::class.java, jsonCalendarSer)
            .registerTypeAdapter(GregorianCalendar::class.java, jsonCalendarSer)
            .registerTypeAdapter(Calendar::class.java, jsonCalendarDeser)
            .create()
    }


    companion object {
        enum class Status {
            SUCCESS,
            WRONG_ACTION,
            NO_ACCESS_TO_FILE,
            NO_BACKUP_FILE,
            WRONG_PASSWORD,
            NO_PASSWORD
        }
    }
}