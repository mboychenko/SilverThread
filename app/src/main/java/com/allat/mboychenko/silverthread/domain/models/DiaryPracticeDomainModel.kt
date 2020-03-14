package com.allat.mboychenko.silverthread.domain.models

import com.allat.mboychenko.silverthread.presentation.helpers.trimRedurantWhitespaces
import java.util.*

class DiaryPracticeDomainModel(
    val id: String = UUID.randomUUID().toString(),
    title: String = "",
    val start: Calendar = Calendar.getInstance(),
    val end: Calendar? = null,
    val duration: Long = 0,
    val notes: String? = null
) {

    var title: String = title.trimRedurantWhitespaces()
        set(value) {
            field = value.trimRedurantWhitespaces()
        }
}