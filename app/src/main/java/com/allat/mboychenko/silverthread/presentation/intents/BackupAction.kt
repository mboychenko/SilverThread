package com.allat.mboychenko.silverthread.presentation.intents

sealed class BackupAction {
    class IntervalAction(val interval: Int) : BackupAction()
    class Restore(val pwd: String) : BackupAction()
    class ResetPwdAndBackup(val pwd: String) : BackupAction()
    class SetupPwd(val pwd: String) : BackupAction()
    class StoragePermissionStateUpdate(val granted: Boolean) : BackupAction()
    object BindInitAction : BackupAction()
    object Backup : BackupAction()
    object BackupPwdReqAction : BackupAction()
    object StoragePermissionRequest : BackupAction()
    object FinishProcess : BackupAction()
}