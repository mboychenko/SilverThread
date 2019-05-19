package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File

/* Checks if external storage is available for read and write */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/* Checks if external storage is available to at least read */
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

fun writeExStoragePermissionGranted(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        return false
    }
    return true
}

fun getExternalStorageAvailableSpaceBytes() =
    Environment.getExternalStorageDirectory().freeSpace

fun getPublicDownloadsStorageDir(childFolder: String): File? {
    val file = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), childFolder)

    if (!file.exists() && !file.mkdirs()) {
        Log.d(LOG_TAG, "Directory not created: ${file.path}")
        return null
    }

    return file
}

const val LOG_TAG = "TAG_FILE_HELPER"
