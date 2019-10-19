package com.allat.mboychenko.silverthread.presentation.helpers

import androidx.annotation.StringRes
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.data.models.AllatTimeZone
import java.util.*

fun getNextStageTime(): Pair<VerdictStages, Calendar> {
    val verdictDate = Calendar.getInstance(AllatTimeZone.GMT.getCalendarTimezone())
    var verdictStage = VerdictStages.IN_PROGRESS
    verdictDate.set(2024, 11, 21, 9, 0, 0)

    if (Calendar.getInstance().timeInMillis > verdictDate.timeInMillis) {
        verdictDate.set(Calendar.YEAR, 2036)
        verdictStage = VerdictStages.IN_EXECUTION
    }

    return verdictStage to verdictDate
}

enum class VerdictStages(@StringRes val resId: Int) {
    IN_PROGRESS(R.string.to_verdict),
    IN_EXECUTION(R.string.verdict_in_exec)
}