package com.allat.mboychenko.silverthread.domain.helper

import android.content.Context
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.BooksConstants

class BooksHelper(val context: Context) {

    fun getAllBooks() = listOf(
        BooksConstants.ALLAT_RA,
        BooksConstants.SENSEI_1,
        BooksConstants.SENSEI_2,
        BooksConstants.SENSEI_3,
        BooksConstants.SENSEI_4,
        BooksConstants.EZOOSMOS,
        BooksConstants.BIRDS_AND_STONE,
        BooksConstants.CROSSROADS,
        BooksConstants.UNI_GRAIN,
        BooksConstants.CONSCIOUSNESS_AND_PERSONALITY,
        BooksConstants.PHYSICS,
        BooksConstants.CLIMATE
    )

    fun getBookTitle(book: BooksConstants.Book) =
        when(book) {
            BooksConstants.ALLAT_RA -> context.getString(R.string.allatra_book_name)
            BooksConstants.SENSEI_1 -> context.getString(R.string.sensei_1_book_name)
            BooksConstants.SENSEI_2 -> context.getString(R.string.sensei_2_book_name)
            BooksConstants.SENSEI_3 -> context.getString(R.string.sensei_3_book_name)
            BooksConstants.SENSEI_4 -> context.getString(R.string.sensei_4_book_name)
            BooksConstants.EZOOSMOS -> context.getString(R.string.ezoosmos_book_name)
            BooksConstants.BIRDS_AND_STONE -> context.getString(R.string.birds_and_stone_book_name)
            BooksConstants.CROSSROADS -> context.getString(R.string.crossroads_book_name)
            BooksConstants.UNI_GRAIN -> context.getString(R.string.uni_grain_book_name)
            BooksConstants.CONSCIOUSNESS_AND_PERSONALITY -> context.getString(R.string.con_and_per_book_name)
            BooksConstants.PHYSICS -> context.getString(R.string.physics_book_name)
            BooksConstants.CLIMATE -> context.getString(R.string.climate_book_name)
            else -> ""
        }

    fun getBookByUrl(url: String) =
        getAllBooks().find { it.localeDetails.containsValue(BooksConstants.BookDetails(url)) } ?: BooksConstants.EMPTY

    fun getBookByFileName(fileName: String) =
        getAllBooks().find { it.fileName == fileName } ?: BooksConstants.EMPTY

}