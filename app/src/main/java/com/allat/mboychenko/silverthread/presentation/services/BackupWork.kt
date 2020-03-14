package com.allat.mboychenko.silverthread.presentation.services

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.allat.mboychenko.silverthread.data.storage.preferences.SensitiveStorage
import com.allat.mboychenko.silverthread.domain.helper.BackupHelper
import com.allat.mboychenko.silverthread.domain.helper.BackupHelper.Companion.Status
import com.allat.mboychenko.silverthread.domain.interactor.AppSettingsStorage
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.text.Charsets.UTF_8

class BackupWork(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    override suspend fun doWork(): Result {
        val backupHelper: BackupHelper by inject()
        val backupInfoStorage: SensitiveStorage by inject()
        val appSettingsStorage: AppSettingsStorage by inject()

        val action = inputData.getString(BACKUP_WORK_ACTION_ARG)?.let { arg ->
            Action.values().find { it.name == arg }
        }

        val code = when (action) {
            Action.BACKUP_AUTO -> {
                val interval = appSettingsStorage.getBackupInterval()
                val lastBackupTime = appSettingsStorage.getLastBackupTime()
                 if (shouldSkip(interval, lastBackupTime)) {
                     return Result.success()
                 }
                backupInfoStorage.getBackupPwd()
            }
            Action.BACKUP -> {
                extractKey(inputData) ?: backupInfoStorage.getBackupPwd()
            }
            Action.RESTORE -> {
                extractKey(inputData)
            }
            else -> return Result.failure(workDataOf(BACKUP_WORK_RESULT_KEY to Status.WRONG_ACTION.name))
        }

        if (code.isNullOrEmpty()) {
            return Result.failure(workDataOf(BACKUP_WORK_RESULT_KEY to Status.NO_PASSWORD.name))
        }

        setProgress(workDataOf(BACKUP_WORK_ACTION_ARG to action.name))

        val result: Status = if (action == Action.RESTORE) {
            backupHelper.restore(code)
        } else {
            backupHelper.backup(code).also { status ->
                if (status == Status.SUCCESS) {
                    appSettingsStorage.updateLastBackupTime()
                }
            }
        }

        return when (result) {
            Status.SUCCESS -> Result.success(workDataOf(BACKUP_WORK_ACTION_ARG to action.name))
            else -> Result.failure(
                workDataOf(
                    BACKUP_WORK_ACTION_ARG to action.name,
                    BACKUP_WORK_RESULT_KEY to result.name
                )
            )
        }
    }

    private fun extractKey(inputData: Data): String? {
        return inputData.getByteArray(BACKUP_WORK_PWD_ARG)?.toString(UTF_8)
    }

    /**
     *
     * 0 - never
     * 1 - daily
     * 2 - weekly
     * 3 - monthly
     *
     */
    private fun shouldSkip(interval: Int, lastBackupTime: Long): Boolean {
        if (interval > 0 && lastBackupTime == 0L) {
            return false
        }

        val diffTime =
            Calendar.getInstance().timeInMillis - lastBackupTime + TimeUnit.HOURS.toMillis(1) //1 hour offset
        return interval == 0 ||
        interval == 1 && diffTime < TimeUnit.DAYS.toMillis(1) ||
        interval == 2 && diffTime < TimeUnit.DAYS.toMillis(7) ||
        interval == 3 && diffTime < TimeUnit.DAYS.toMillis(30)
    }

    companion object {
        const val BACKUP_WORK_ACTION_ARG = "BACKUP_WORK_ACTION_ARG"
        const val BACKUP_WORK_PWD_ARG = "BACKUP_WORK_PWD_ARG"
        const val BACKUP_WORK_RESULT_KEY = "BACKUP_WORK_RESULT_KEY"
        private const val BACKUP_WORK_TAG = "BACKUP_WORK_TAG"

        enum class Action {
            BACKUP_AUTO,
            BACKUP,
            RESTORE
        }

        fun enqueueWork(workManager: WorkManager, action: Action, code: String? = null) {
            val data = if (code == null) {
                workDataOf(BACKUP_WORK_ACTION_ARG to action.name)
            } else {
                val pwd = code.toByteArray(UTF_8)
                workDataOf(
                    BACKUP_WORK_PWD_ARG to pwd,
                    BACKUP_WORK_ACTION_ARG to action.name
                )
            }

            val work =
                OneTimeWorkRequest
                    .Builder(BackupWork::class.java)
                    .addTag(BACKUP_WORK_TAG)
                    .setInputData(data)

            workManager.enqueueUniqueWork(BACKUP_WORK_TAG, ExistingWorkPolicy.REPLACE, work.build())
        }

        fun getAction(work: WorkInfo): Action? {
            val rawAction = work.progress.getString(BACKUP_WORK_ACTION_ARG) ?:
                work.outputData.getString(BACKUP_WORK_ACTION_ARG)
            return Action.values().find { it.name == rawAction }
        }

        fun getError(work: WorkInfo): Status? {
            val rawStatus = work.outputData.getString(BACKUP_WORK_RESULT_KEY)
            return Status.values().find { it.name == rawStatus }
        }

        fun getLiveBackupWorkInfo(workManager: WorkManager): LiveData<List<WorkInfo>> =
            workManager.getWorkInfosByTagLiveData(BACKUP_WORK_TAG)

    }

}