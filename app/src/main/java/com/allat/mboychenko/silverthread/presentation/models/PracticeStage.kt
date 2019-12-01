package com.allat.mboychenko.silverthread.presentation.models

import android.content.Context
import androidx.annotation.StringRes
import com.allat.mboychenko.silverthread.R

enum class PracticeStage(@StringRes val descValueId: Int? = null) {
    INIT(null),
    START(R.string.practice_stage_start),
    ALLAT(R.string.allat);

    fun getStageDesc(context: Context, allat: Int = 1): String =
        when (this) {
            INIT -> ""
            START -> context.getString(descValueId!!)
            ALLAT -> String.format("%d %s", allat, context.getString(descValueId!!))
        }
}

