package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models

import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.models.BooksConstants.BooksLocale.*
import java.io.Serializable

object BooksConstants {

    val ALLAT_RA = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/1/en", 24210005),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/1/ru", 17322970),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/1/cz", 6943666),
            IT to BookDetails("https://books.allatra.org/books/getfile/pdf/1/it", 10628167),
            FR to BookDetails("https://books.allatra.org/books/getfile/pdf/1/fr", 12426843),
            DE to BookDetails("https://books.allatra.org/books/getfile/pdf/1/de", 1714599),
            UA to BookDetails("https://books.allatra.org/books/getfile/pdf/1/ua", 11408219),
            UZ to BookDetails("https://books.allatra.org/books/getfile/pdf/1/uz", 5920099),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/1/bg", 18501183)
        ),
        R.drawable.allatra_book,
        "AllatRa.pdf"
    )

    val SENSEI_1 = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/2/en", 2936710),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/2/ru", 22060766),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/2/bg", 3312889),
            DE to BookDetails("https://books.allatra.org/books/getfile/pdf/2/de", 4092366),
            PL to BookDetails("https://books.allatra.org/books/getfile/pdf/2/pl", 9492611),
            LV to BookDetails("https://books.allatra.org/books/getfile/pdf/2/lv", 4988031),
            IT to BookDetails("https://books.allatra.org/books/getfile/pdf/2/it", 1862297),
            TR to BookDetails("https://books.allatra.org/books/getfile/pdf/2/tr", 2042962),
            FR to BookDetails("https://books.allatra.org/books/getfile/pdf/2/fr", 2344599),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/2/cz", 3639957)
        ),
        R.drawable.sensei_i,
        "Sensei 1.pdf"
    )

    val SENSEI_2 = Book(
        mapOf(
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/3/bg", 8438718),
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/3/en", 739981),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/3/ru", 22719030),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/3/cz", 3718867)
        ),
        R.drawable.sensei_ii,
        "Sensei 2.pdf"
    )

    val SENSEI_3 = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/4/en", 919477),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/4/ru", 21106662),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/4/bg", 12681675),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/4/cz", 4093137)
        ),
        R.drawable.sensei_iii,
        "Sensei 3.pdf"
    )

    val SENSEI_4 = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/5/en", 2680318),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/5/ru", 16003734),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/5/bg", 6326124),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/5/cz", 6093318)
        ),
        R.drawable.sensei_iv,
        "Sensei 4.pdf"
    )

    val EZOOSMOS = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/7/en", 1159258),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/7/ru", 4488227),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/7/bg", 2954118),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/7/cz", 4444478)
        ),
        R.drawable.ezoosmos,
        "Ezoosmos.pdf"
    )

    val BIRDS_AND_STONE = Book(
        mapOf(
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/6/en", 1041798),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/6/ru", 9867420),
            UA to BookDetails("https://books.allatra.org/books/getfile/pdf/6/ua", 944387),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/6/bg", 18752640),
            LV to BookDetails("https://books.allatra.org/books/getfile/pdf/6/lv", 15237924),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/6/cz", 3189439)
        ),
        R.drawable.bird_stone,
        "Birds And Stone.pdf"
    )

    val CROSSROADS = Book(
        mapOf(
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/8/bg", 3960090),
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/8/ru", 1771239),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/8/cz", 4049639)
        ),
        R.drawable.crossroads,
        "Crossroad.pdf"
    )

    val UNI_GRAIN = Book(
        mapOf(
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/24/ru", 4335878)
        ),
        R.drawable.uni_grain,
        "Universal Grain.pdf"
    )

    val PHYSICS = Book(
        mapOf(
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/11/ru", 67562511),
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/11/en", 67292993),
            UA to BookDetails("https://books.allatra.org/books/getfile/pdf/11/ua", 61402512),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/11/bg", 70439568),
            UZ to BookDetails("https://books.allatra.org/books/getfile/pdf/11/uz", 4913451),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/11/cz", 60059931)
        ),
        R.drawable.physics,
        "Physics.pdf"
    )

    val CLIMATE = Book(
        mapOf(
            RU to BookDetails("https://books.allatra.org/books/getfile/pdf/25/ru", 44663327),
            EN to BookDetails("https://books.allatra.org/books/getfile/pdf/25/en", 45241265),
            CZ to BookDetails("https://books.allatra.org/books/getfile/pdf/25/cz", 45651616),
            BG to BookDetails("https://books.allatra.org/books/getfile/pdf/25/bg", 43993083),
            DE to BookDetails("https://books.allatra.org/books/getfile/pdf/25/de", 6384693),
            KO to BookDetails("https://books.allatra.org/books/getfile/pdf/25/ko", 13361579),
            AM to BookDetails("https://books.allatra.org/books/getfile/pdf/25/am", 2779434),
            ES to BookDetails("https://books.allatra.org/books/getfile/pdf/25/es", 3900844)
        ),
        R.drawable.climate,
        "Climate.pdf"
    )

    val EMPTY = Book()

    class Book(val localeDetails: Map<BooksLocale, BookDetails> = emptyMap(),
               val imageRes: Int = 0, val fileName: String = "")

    class BookDetails(val url: String = "", val bookSizeBytes : Long = 0) {
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

    enum class BooksLocale(val language: String) {
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
