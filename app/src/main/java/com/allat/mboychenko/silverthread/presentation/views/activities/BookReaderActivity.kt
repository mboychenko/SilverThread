package com.allat.mboychenko.silverthread.presentation.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.domain.interactor.BooksLoaderDetailsStorage
import kotlinx.android.synthetic.main.activity_reader.*
import org.koin.android.ext.android.inject
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import java.util.*


class BookReaderActivity : AppCompatActivity() {

    private val storage: BooksLoaderDetailsStorage by inject()
    private var timer = Timer()
    private lateinit var bookName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        close.setOnClickListener { finish() }

        bookName = intent.getStringExtra(BOOK_NAME_ARG)

        pdfView.fromUri(intent.data)
            .onPageChange(onPageChangeListener)
            .defaultPage(storage.getLastBookPage(bookName))
            .load()
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