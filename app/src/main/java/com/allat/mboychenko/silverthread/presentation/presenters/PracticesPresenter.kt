package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.fragments.IPracticesFragmentView
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeItem

class PracticesPresenter(val context: Context) : BasePresenter<IPracticesFragmentView>() {

    override fun attachView(view: IPracticesFragmentView) {
        super.attachView(view)
        if (!view.hasPractices()) {
            preparePracticesItems()
        }
    }

    private fun preparePracticesItems() {
        val practices = mutableListOf<PracticeItem>()
        practices.add(
            PracticeItem(
                R.string.shultz_autoreport_title,
                R.string.shultz_autoreport,
                R.drawable.autogen_bg,
                PracticeItem.PracticesType.AUTOREPORTS
            )
        )

        practices.add(
            PracticeItem(
                R.string.altered_consciousness_state_title,
                R.string.altered_consciousness_state,
                R.drawable.autogen_bg,//todo
                PracticeItem.PracticesType.MEDITATIONS
            )
        )

        practices.add(
            PracticeItem(
                R.string.concetration_of_attention_title,
                R.string.concetration_of_attention,
                R.drawable.autogen_bg,//todo
                PracticeItem.PracticesType.MEDITATIONS
            )
        )

        practices.add(
            PracticeItem(
                R.string.lotus_title,
                R.string.lotus,
                R.drawable.autogen_bg,//todo
                PracticeItem.PracticesType.SPIRITUAL
            )
        )

        view?.onPracticesByCategoryReady(practices)
    }


}