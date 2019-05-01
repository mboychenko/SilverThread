package com.allat.mboychenko.silverthread.presentation.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.Storage
import kotlinx.android.synthetic.main.activity_reader.*
import org.koin.android.ext.android.inject
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import java.util.*


class BookReaderActivity : AppCompatActivity() {

    private val storage: Storage by inject() //todo move to presenter
    private var timer = Timer()
    private val DELAY: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.allat.mboychenko.silverthread.R.layout.activity_reader)

        pdfView.fromUri(intent.data)
//            .onPageChange { page, _ -> storage.putInt(BOOK_CURRENT_PAGE, page) } //check how fast it can be, add if need debounce
            .onPageChange(onPageChangeListener) //check how fast it can be, add if need debounce
            .defaultPage(storage.getInt(BOOK_CURRENT_PAGE))
            .load()
    }

    private val onPageChangeListener = OnPageChangeListener { page, _ ->
        timer.cancel()
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                storage.putInt(BOOK_CURRENT_PAGE, page)
            }

        }, DELAY)
    }

    companion object {
        private const val BOOK_CURRENT_PAGE = "BOOK_CURRENT_PAGE"
    }
}