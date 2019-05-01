package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models

import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.BooksConstants.BooksLocale.*
import java.io.Serializable

object BooksConstants {

    val ALLAT_RA = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/1/en", 1233),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/1/ru", 1234),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/1/cz", 123),
            IT to BookDetails("https://books.allatra.org/books/getfile/pdf/1/it", 123),
            FR to BookDetails("https://books.allatra.org/books/getfile/pdf/1/fr", 123),
            DE to BookDetails("https://books.allatra.org/books/getfile/pdf/1/de", 123),
            UA to BookDetails("https://books.allatra.org/books/getfile/pdf/1/ua", 123),
            UZ to BookDetails("https://books.allatra.org/books/getfile/pdf/1/uz", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/1/bg", 123)
        ),
        R.drawable.allatra_book,
        "AllatRa.pdf"
    )

    val SENSEI_1 = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/2/en", 1234),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/2/ru", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/2/bg", 123),
            DE to BookDetails("https://books.allatra.org/books/getfile/pdf/2/de", 123),
            PL to BookDetails("https://books.allatra.org/books/getfile/pdf/2/pl", 123),
            LV to BookDetails("https://books.allatra.org/books/getfile/pdf/2/lv", 123),
            IT to BookDetails("https://books.allatra.org/books/getfile/pdf/2/it", 123),
            TR to BookDetails("https://books.allatra.org/books/getfile/pdf/2/tr", 123),
            FR to BookDetails("https://books.allatra.org/books/getfile/pdf/2/fr", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/2/cz", 123)
        ),
        R.drawable.sensei_i,
        "Sensei 1.pdf"
    )

    val SENSEI_2 = Book(
        mapOf(
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/3/bg", 123),
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/3/en", 123),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/3/ru", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/3/cz", 123)
        ),
        R.drawable.sensei_ii,
        "Sensei 2.pdf"
    )

    val SENSEI_3 = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/4/en", 123),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/4/ru", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/4/bg", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/4/cz", 123)
        ),
        R.drawable.sensei_iii,
        "Sensei 3.pdf"
    )

    val SENSEI_4 = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/5/en", 123),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/5/ru", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/5/bg", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/5/cz", 123)
        ),
        R.drawable.sensei_iv,
        "Sensei 4.pdf"
    )

    val EZOOSMOS = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/7/en", 123),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/7/ru", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/7/bg", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/7/cz", 123)
        ),
        R.drawable.ezoosmos,
        "Ezoosmos.pdf"
    )

    val BIRDS_AND_STONE = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/6/en", 123),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/6/ru", 123),
            UA to BookDetails("https://books.allatra.org/books/getfile/pdf/6/ua", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/6/bg", 123),
            LV to BookDetails("https://books.allatra.org/books/getfile/pdf/6/lv", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/6/cz", 123)
        ),
        R.drawable.bird_stone,
        "Birds And Stone.pdf"
    )

    val CROSSROADS = Book(
        mapOf(
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/8/bg", 123),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/8/ru", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/8/cz", 123)
        ),
        R.drawable.crossroads,
        "Crossroad.pdf"
    )

    val UNI_GRAIN = Book(
        mapOf(
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/24/ru", 123)
        ),
        R.drawable.uni_grain,
        "Universal Grain.pdf"
    )

    val PHYSICS = Book(
        mapOf(
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/11/ru", 123),
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/11/en", 123),
            UA to BookDetails("https://books.allatra.org/books/getfile/pdf/11/ua", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/11/bg", 123),
            UZ to BookDetails("https://books.allatra.org/books/getfile/pdf/11/uz", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/11/cz", 123)
        ),
        R.drawable.physics,
        "Physics.pdf"
    )

    val CLIMATE = Book(
        mapOf(
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/25/ru", 123),
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/25/en", 123),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/25/cz", 123),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/25/bg", 123),
            DE to BookDetails("https://books.allatra.org/books/getfile/pdf/25/de", 123),
            KO to BookDetails("https://books.allatra.org/books/getfile/pdf/25/ko", 123),
            AM to BookDetails("https://books.allatra.org/books/getfile/pdf/25/am", 123),
            ES to BookDetails("https://books.allatra.org/books/getfile/pdf/25/es", 123)
        ),
        R.drawable.climate,
        "Climate.pdf"
    )

    val EMPTY = Book()

    class Book(val localeDetails: Map<BooksLocale, BookDetails> = emptyMap(),
               val imageRes: Int = 0, val fileName: String = "")

    class BookDetails(val url: String = "", val bookSize : Long = 0) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BookDetails

            if (url != other.url) return false

            return true
        }

        override fun hashCode(): Int {
            return url.hashCode()
        }
    }

    enum class BooksLocale(val language: String): Serializable {
        RU("Русский"),
        EN("English"),
        BG("Български"),
        IT("Italiano"),
        FR("Français"),
        DE("Deutsch"),
        UA("Українська"),
        UZ("Ўзбек"),
        PL("Polski"),
        LV("Latviešu"),
        TR("Türkçe"),
        KO("Korean"),
        AM("አማርኛ"),
        ES("Español"),
        CZ("Čeština");

        override fun toString(): String {
            return language
        }

    }

    fun getLocales() = listOf(RU, EN, BG, IT, FR, DE, UA, UZ, PL, LV, TR, KO, AM, ES, CZ)
}
