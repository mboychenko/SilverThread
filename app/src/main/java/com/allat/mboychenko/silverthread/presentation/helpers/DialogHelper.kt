package com.allat.mboychenko.silverthread.presentation.helpers

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.allat.mboychenko.silverthread.R

fun getConfirmationDialog(context: Context, title: String, action: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.delete_confirmation, title))
        .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
            action()
        }
        .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        .show()
}