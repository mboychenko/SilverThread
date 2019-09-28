package com.allat.mboychenko.silverthread.presentation.views.listitems

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
                context.startActivity(intent)
            }
        }


    }

    override fun getLayout() = R.layout.saved_file_item_layout


}