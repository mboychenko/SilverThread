package com.allat.mboychenko.silverthread.data.storage.preferences

import android.content.Context
import android.preference.PreferenceManager

/**
 * Real implementation hidden
 */
class SensitiveStorageImpl(
    context: Context
) : StorageImplementation(
    context,
    PreferenceManager.getDefaultSharedPreferences(context)
), SensitiveStorage {

    override fun getBackupPwd(): String? {
        return getString(BACKUP_PSWD_KEY)
    }

    override fun saveBackupPwd(value: String) {
        putString(BACKUP_PSWD_KEY, value)
    }

    override fun hasBackupPwd(): Boolean = contains(BACKUP_PSWD_KEY)

    companion object {
        private const val BACKUP_PSWD_KEY = "BACKUP_PSWD_KEY"
    }
}