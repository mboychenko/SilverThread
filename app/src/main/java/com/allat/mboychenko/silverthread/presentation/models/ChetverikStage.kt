package com.allat.mboychenko.silverthread.presentation.models

import android.content.Context
import androidx.annotation.StringRes
import com.allat.mboychenko.silverthread.R

enum class ChetverikStage(@StringRes val descValueId: Int? = null) {
    INIT(null),
    START(R.string.chetverik_stage_start),
    RIGHT(R.string.chetverik_stage_right),
    LEFT(R.string.chetverik_stage_left),
    BACK(R.string.chetverik_stage_back),
    FRONT(R.string.chetverik_stage_front),
    END(R.string.chetverik_end);

    fun getChetverikStageDesc(context: Context): String =
        when (this) {
            INIT -> ""
            END,
            START -> context.getString(descValueId!!)
            RIGHT,
            LEFT,
            BACK,
            FRONT -> context.getString(R.string.chetverik_stage, context.getString(descValueId!!))
        }
}

