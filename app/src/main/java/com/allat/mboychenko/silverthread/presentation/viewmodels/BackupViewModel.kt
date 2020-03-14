package com.allat.mboychenko.silverthread.presentation.viewmodels

import android.content.Context
import android.os.Environment
import androidx.lifecycle.*
import androidx.work.*
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.storage.preferences.SensitiveStorage
import com.allat.mboychenko.silverthread.domain.helper.BackupHelper.Companion.Status
import com.allat.mboychenko.silverthread.domain.helper.BackupHelper.Companion.Status.*
import com.allat.mboychenko.silverthread.domain.interactor.AppSettingsStorage
import com.allat.mboychenko.silverthread.presentation.helpers.BACKUP_FOLDER_NAME
import com.allat.mboychenko.silverthread.presentation.helpers.extStoragePermissionGranted
import com.allat.mboychenko.silverthread.presentation.helpers.isExternalStorageAvailable
import com.allat.mboychenko.silverthread.presentation.intents.BackupAction
import com.allat.mboychenko.silverthread.presentation.services.BackupWork
import com.allat.mboychenko.silverthread.presentation.services.BackupWork.Companion.Action
import com.allat.mboychenko.silverthread.presentation.viewstate.BackupViewState
import kotlinx.coroutines.launch

class BackupViewModel(
    private val context: Context,
    private val workerManager: WorkManager,
    private val secureStorage: SensitiveStorage,
    private val appSettingsStorage: AppSettingsStorage
) : ViewModel() {

    private val intentAction = MutableLiveData<BackupAction>(BackupAction.BindInitAction)

    private val backupWorkLiveInfo = BackupWork.getLiveBackupWorkInfo(workerManager)

    private val mediatorState = object : MediatorLiveData<BackupViewState>() {
        init {
            @Suppress("ControlFlowWithEmptyBody")
            addSource(backupWorkLiveInfo) {
                if (it.isNotEmpty()) {
                    val workInfo = it[0]
                    if (workInfo.state.isFinished && value?.actionState == null) {
                        //init info from last work
                    } else {
                        val action = BackupWork.getAction(workInfo)
                        val errorStatus = BackupWork.getError(workInfo)

                        val actionState =
                            when {
                                workInfo.state.isFinished -> {
                                    val result =
                                        getResultMsgDependOn(workInfo.state, action, errorStatus)
                                    BackupViewState.ActionState(inProgress = false, resultMsg = result)
                                }
                                workInfo.state == WorkInfo.State.RUNNING -> {
                                    val result = getProgressMsgDependOn(action)
                                    BackupViewState.ActionState(inProgress = true, resultMsg = result)
                                }
                                else -> {
                                    null
                                }
                            }

                        value = if (errorStatus == NO_ACCESS_TO_FILE) {
                            value?.copy(
                                actionState = null,
                                reqStoragePerm = false,
                                noStoragePerm = true
                            ) ?: BackupViewState(noStoragePerm = true)
                        } else {
                            value?.copy(actionState = actionState)
                                ?: BackupViewState(actionState = actionState)
                        }
                    }
                }
            }

            addSource(intentAction) {
                when (it) {
                    is BackupAction.IntervalAction -> {
                        if (value?.backupInterval != it.interval) {
                            setInterval(it.interval)
                        }
                    }
                    is BackupAction.SetupPwd -> {
                        value = value?.copy(backupPwdReq = false) ?: BackupViewState()
                        setupPassword(it.pwd)
                    }
                    is BackupAction.BackupPwdReqAction -> {
                        value =
                            value?.copy(backupPwdReq = true) ?: BackupViewState(backupPwdReq = true)
                    }
                    is BackupAction.FinishProcess -> {
                        value = value?.copy(actionState = null) ?: BackupViewState()
                    }
                    is BackupAction.ResetPwdAndBackup -> resetPasswordAndBackup(it.pwd)
                    is BackupAction.Restore -> {
                        restore(it.pwd)
                    }
                    is BackupAction.Backup -> backup()
                    is BackupAction.StoragePermissionRequest -> {
                        value = value?.copy(reqStoragePerm = true) ?: BackupViewState(reqStoragePerm = true)
                    }
                    is BackupAction.StoragePermissionStateUpdate -> {
                        value = value?.copy(reqStoragePerm = false, noStoragePerm = !it.granted)
                            ?: BackupViewState(noStoragePerm = !it.granted)
                    }
                    is BackupAction.BindInitAction -> {
                        viewModelScope.launch {
                            val reqPerm =
                                isExternalStorageAvailable().not() ||
                                        extStoragePermissionGranted(context).not()
                            val hasKey = secureStorage.hasBackupPwd()
                            val interval = appSettingsStorage.getBackupInterval()
                            value = value?.copy(
                                reqStoragePerm = reqPerm,
                                backupInterval = interval,
                                backupPwdReq = !hasKey
                            ) ?: BackupViewState(
                                reqStoragePerm = reqPerm,
                                backupInterval = interval,
                                backupPwdReq = !hasKey
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getProgressMsgDependOn(action: Action?): String {
        return when(action) {
            Action.RESTORE -> context.getString(R.string.restore_in_progress)
            Action.BACKUP_AUTO,
            Action.BACKUP -> context.getString(R.string.backup_in_progress)
            else -> context.getString(R.string.in_progress)
        }
    }

    private fun getResultMsgDependOn(state: WorkInfo.State, action: Action?, errorStatus: Status?): String {
        return when {
            state == WorkInfo.State.SUCCEEDED && action == Action.RESTORE -> {
                context.getString(R.string.success_restore)
            }
            state == WorkInfo.State.SUCCEEDED &&
                    (action == Action.BACKUP_AUTO || action == Action.BACKUP) -> {
                context.getString(R.string.success_backup)
            }
            state == WorkInfo.State.FAILED && errorStatus != null -> {
                return when (errorStatus) {
                    NO_ACCESS_TO_FILE -> context.getString(R.string.cant_backup_no_storage_permission)
                    NO_BACKUP_FILE -> context.getString(
                        R.string.failed_restore_no_backup_file,
                        "../${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_FOLDER_NAME/"
                    )
                    WRONG_PASSWORD -> context.getString(R.string.failed_restore_wrong_password)
                    else -> context.getString(R.string.finished_with_error)
                }
            }
            else -> context.getString(R.string.finished)
        }
    }

    fun bind(): LiveData<BackupViewState> = mediatorState

    fun intent(intent: BackupAction) {
        intentAction.value = intent
    }

    private fun setInterval(value: Int) {
        viewModelScope.launch {
            appSettingsStorage.setBackupInterval(value)
            if (!secureStorage.hasBackupPwd()) {
                intent(BackupAction.BackupPwdReqAction)
            }
        }
    }

    private fun restore(pwd: String) {
        BackupWork.enqueueWork(workerManager, Action.RESTORE, pwd)
    }

    private fun backup() {
        viewModelScope.launch {
            if (!secureStorage.hasBackupPwd()) {
                intent(BackupAction.BackupPwdReqAction)
                return@launch
            } else {
                BackupWork.enqueueWork(workerManager, Action.BACKUP)
            }
        }
    }

    private fun resetPasswordAndBackup(pwd: String) {
        viewModelScope.launch {
            secureStorage.saveBackupPwd(pwd)
            BackupWork.enqueueWork(workerManager, Action.BACKUP, pwd)
        }
    }

    private fun setupPassword(pwd: String) {
        viewModelScope.launch {
            secureStorage.saveBackupPwd(pwd)
        }
    }
}