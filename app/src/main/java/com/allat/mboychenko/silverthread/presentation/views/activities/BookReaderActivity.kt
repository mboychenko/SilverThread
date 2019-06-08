package com.allat.mboychenko.silverthread.presentation.views.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_reader.*
import org.koin.android.ext.android.inject
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import java.util.*
import android.widget.EditText
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.custom.InputFilterMinMax
import com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage


class BookReaderActivity : AppCompatActivity() {

    private val storage: BooksLoaderDetailsStorage by inject()
    private var timer = Timer()
    private lateinit var bookName: String
    private lateinit var selectPageDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        close.setOnClickListener { finish() }

        bookName = intent.getStringExtra(BOOK_NAME_ARG)

        pdfView.fromUri(intent.data)
            .onPageChange(onPageChangeListener)
            .defaultPage(storage.getLastBookPage(bookName))
            .onLoad { pageSelectDialog() }
            .load()


        page.setOnClickListener {
            if(::selectPageDialog.isInitialized) {
                selectPageDialog.show()
            }
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

    companion object {
        const val BOOK_NAME_ARG = "BOOK_NAME_ARG"
        private const val PAGE_SAVE_DELAY = 2000L
    }
}