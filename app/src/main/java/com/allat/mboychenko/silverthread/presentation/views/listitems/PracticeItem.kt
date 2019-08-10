package com.allat.mboychenko.silverthread.presentation.views.listitems
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.dialogs.PracticeFormattedDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.PracticeFormattedDialog.Companion.PRACTICES_DIALOG_TAG
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import com.allat.mboychenko.silverthread.presentation.helpers.htmlFromAssetsCompat


class PracticeItem(
    private val title: String,
    private val text: String,
    private val imageDrawableRes: Int,
    val type: PracticesType
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        viewHolder.itemView.findViewById<TextView>(R.id.title).text = title
        viewHolder.itemView.findViewById<TextView>(R.id.text).text = htmlFromAssetsCompat(text)

        viewHolder.itemView.setOnClickListener { showDialog(context) }
    }

    override fun getLayout(): Int {
        return R.layout.practice_item_layout
    }

    private fun showDialog(context: Context) {
        PracticeFormattedDialog
            .newInstance(title, text)
            .show((context as FragmentActivity).supportFragmentManager, PRACTICES_DIALOG_TAG)
    }

    private fun getDrawable(context: Context, res: Int) = ContextCompat.getDrawable(context, res)

    enum class PracticesType {
        AUTOREPORTS,
        MEDITATIONS,
        SPIRITUAL
    }
}