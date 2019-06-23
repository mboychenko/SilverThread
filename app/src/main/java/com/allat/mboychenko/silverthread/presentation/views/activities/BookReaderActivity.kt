package com.allat.mboychenko.silverthread.presentation.views.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_reader.*
import org.koin.android.ext.android.inject
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import java.util.*
import android.widget.EditText
import android.widget.Toast
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.custom.InputFilterMinMax
import com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import androidx.core.app.ActivityCompat
import com.allat.mboychenko.silverthread.presentation.helpers.extStoragePermissionGranted


class BookReaderActivity : AppCompatActivity() {

    private val storage: BooksLoaderDetailsStorage by inject()
    private var timer = Timer()
    private lateinit var bookName: String
    private lateinit var bookUri: Uri
    private lateinit var selectPageDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        close.setOnClickListener { finish() }

        bookUri = intent.data!!
        bookName = intent.getStringExtra(BOOK_NAME_ARG)

        page.setOnClickListener {
            if(::selectPageDialog.isInitialized) {
                selectPageDialog.show()
            }
        }

        if (extStoragePermissionGranted(this).not()) {
            requestPermissions()
            return
        } else {
            loadBook()
        }
    }

    private fun loadBook() {
        pdfView.fromUri(bookUri)
            .onPageChange(onPageChangeListener)
            .defaultPage(storage.getLastBookPage(bookName))
            .onLoad { pageSelectDialog() }
            .load()
    }

    private fun requestPermissions() {
        try {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e("Permission request fail", e.message)
        }
    }

    private fun pageSelectDialog() {
        val view = layoutInflater.inflate(R.layout.select_page_layout, null)
        val editText = view.findViewById<EditText>(R.id.page_number)
        editText.hint = getString(R.string.select_page_hint, pdfView.pageCount)
        editText.filters = arrayOf(InputFilterMinMax(1, pdfView.pageCount))

        selectPageDialog = AlertDialog.Builder(this)
            .setTitle(R.string.select_page)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (editText.text.isNullOrEmpty().not()) {
                    val page = editText.text.toString().toInt()
                    if (page in 1..pdfView.pageCount) {
                        pdfView.jumpTo(page - 1, true)
                    }
                    editText.text.clear()
                }
            }
            .setOnCancelListener { dialog -> dialog.dismiss() }
            .create()
    }

    private val onPageChangeListener = OnPageChangeListener { page, _ ->
        timer.cancel()
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                storage.saveLastBookPage(bookName, page)
            }

        }, PAGE_SAVE_DELAY)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for ((position, permission) in permissions.withIndex()) {
                val grantResult = grantResults[position]

                if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        loadBook()
                    } else {
                        Toast.makeText(this, "permission not granted", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

    companion object {
        const val BOOK_NAME_ARG = "BOOK_NAME_ARG"
        private const val PAGE_SAVE_DELAY = 2000L
        private const val PERMISSION_REQUEST_CODE = 765
    }
}