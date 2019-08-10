package com.allat.mboychenko.silverthread.presentation.presenters

import android.content.Context
import androidx.annotation.StringRes
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.ExecutorThread
import com.allat.mboychenko.silverthread.presentation.helpers.runTaskOnBackgroundWithResult
import com.allat.mboychenko.silverthread.presentation.views.fragments.IPracticesFragmentView
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeItem
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class PracticesPresenter(val context: Context) : BasePresenter<IPracticesFragmentView>() {

    override fun attachView(view: IPracticesFragmentView) {
        super.attachView(view)
        if (!view.hasPractices()) {
            preparePracticesItems()
        }
    }

    private fun preparePracticesItems() {
        subscriptions.add(
            runTaskOnBackgroundWithResult(
                ExecutorThread.IO,
                {
                    val practices = mutableListOf<PracticeItem>()
                    practices.add(
                        PracticeItem(
                            context.getString(R.string.shultz_autoreport_title),
                            getMeditationFromAssets(context, R.string.shultz_autoreport),
                            imageDrawableRes = R.drawable.autogen_bg,
                            type = PracticeItem.PracticesType.AUTOREPORTS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.altered_consciousness_state_title),
                            getMeditationFromAssets(context, R.string.altered_consciousness_state),
                            imageDrawableRes = R.drawable.autogen_bg,
                            type = PracticeItem.PracticesType.MEDITATIONS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.concetration_of_attention_title),
                            getMeditationFromAssets(context, R.string.concetration_of_attention),
                            imageDrawableRes = R.drawable.autogen_bg,
                            type = PracticeItem.PracticesType.MEDITATIONS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.kuvshin_title),
                            getMeditationFromAssets(context, R.string.kuvshin),
                            R.drawable.autogen_bg,
                            PracticeItem.PracticesType.MEDITATIONS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.lotus_love_bl_title),
                            getMeditationFromAssets(context, R.string.lotus_love_bl),
                            R.drawable.autogen_bg,
                            PracticeItem.PracticesType.MEDITATIONS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.chetverik_title),
                            getMeditationFromAssets(context, R.string.chetverik),
                            R.drawable.autogen_bg,
                            PracticeItem.PracticesType.MEDITATIONS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.pyramid_title),
                            getMeditationFromAssets(context, R.string.pyramid),
                            R.drawable.autogen_bg,
                            PracticeItem.PracticesType.MEDITATIONS
                        )
                    )

                    practices.add(
                        PracticeItem(
                            context.getString(R.string.lotus_title),
                            getMeditationFromAssets(context, R.string.lotus),
                            R.drawable.autogen_bg,
                            PracticeItem.PracticesType.SPIRITUAL
                        )
                    )
                    practices
                },
                {
                    view?.onPracticesByCategoryReady(it)
                })
        )
    }


    private fun getMeditationFromAssets(context: Context, @StringRes resId: Int): String {
        val termsString = StringBuilder()
        val reader: BufferedReader
        try {
            reader = BufferedReader(
                InputStreamReader(context.assets.open(context.getString(resId)))
            )

            var str = reader.readLine()
            while (str != null) {
                termsString.append(str)
                str = reader.readLine()
            }

            reader.close()
            return termsString.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }
}