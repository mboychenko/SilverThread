package com.allat.mboychenko.silverthread.presentation.views.listitems

import android.content.ActivityNotFoundException
import android.widget.TextView
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.*
import java.io.File


class LoadedFileItem(
    private val name: String,
    private val size: String,
    private val filePath: String,
    private val removeItem: (item: LoadedFileItem) -> Unit
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.apply {
            val fileName = findViewById<TextView>(R.id.fileName)
            val fileSize = findViewById<TextView>(R.id.fileSize)
            val delete = findViewById<Button>(R.id.delete)
            fileName.text = name
            fileSize.text = size
            delete.setOnClickListener {
                getConfirmationDialog(context, name) {
                    runTaskOnBackgroundWithResult(
                        ExecutorThread.IO,
                        {
                            val myFile = File(filePath)
                            if (myFile.exists()) {
                                myFile.delete()
                            }
                        },
                        {
                            removeItem(this@LoadedFileItem)
                            Toast.makeText(context, R.string.deleted, Toast.LENGTH_LONG).show()
                        }
                    )
                }

            }

            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                val uri =
                    FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITIES, File(filePath))
                val type = getMimeType(uri, context)
                intent.setDataAndType(uri, type)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.unknown_format, Toast.LENGTH_LONG).show()
                }
            }
        }


    }



    override fun getLayout() = R.layout.saved_file_item_layout

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadedFileItem

        if (name != other.name) return false
        if (size != other.size) return false
        if (filePath != other.filePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + filePath.hashCode()
        return result
    }


}