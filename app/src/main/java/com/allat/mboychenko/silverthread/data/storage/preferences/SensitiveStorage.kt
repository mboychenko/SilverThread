package com.allat.mboychenko.silverthread.data.storage.preferences

interface SensitiveStorage {
    fun getBackupPwd(): String?
    fun saveBackupPwd(value: String)
    fun hasBackupPwd(): Boolean
}