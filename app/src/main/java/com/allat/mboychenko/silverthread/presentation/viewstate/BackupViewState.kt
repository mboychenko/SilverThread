package com.allat.mboychenko.silverthread.presentation.viewstate

data class BackupViewState(
    val backupInterval: Int = 0,
    val actionState: ActionState? = null,
    val backupPwdReq: Boolean = false,
    val reqStoragePerm: Boolean = false,
    val noStoragePerm: Boolean = false
) {
    data class ActionState(val inProgress: Boolean = false, val resultMsg: String? =null)
}