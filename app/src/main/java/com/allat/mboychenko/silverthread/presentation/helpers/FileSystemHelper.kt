package com.allat.mboychenko.silverthread.presentation.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.math.ln
import kotlin.math.pow
import android.webkit.MimeTypeMap
import android.content.ContentResolver
import android.net.Uri


/* Checks if external storage is available for read and write */
fun isExternalStorageAvailable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/* Checks if external storage is available to at least read */
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

fun extStoragePermissionGranted(context: Context): Boolean {
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

fun humanReadableByteCount(bytes: Long): String {
    val unit =  1024
    if (bytes < unit) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = "KMGTPE"[exp - 1] + "i"
    return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
}

fun getMimeType(uri: Uri, context: Context): String? {
    return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val cr = context.contentResolver
        cr.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
    }
}

const val WEB_DOWNLOADS_FOLDER_NAME = "AllatRa Downloads"
const val FILE_PROVIDER_AUTHORITIES = "com.allat.mboychenko.silverthread"
const val LOG_TAG = "TAG_FILE_HELPER"
