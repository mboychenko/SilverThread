package com.allat.mboychenko.silverthread.presentation.views.viewholders

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.models.ItemIdentifiable
import com.allat.mboychenko.silverthread.presentation.views.listeners.INoteActionListener

abstract class DiaryBaseViewHolder<T: ItemIdentifiable>(
    parentView: ViewGroup,
    private val actionListener: INoteActionListener,
    layoutId: Int
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parentView.context).inflate(layoutId, parentView, false)
) {
    protected lateinit var menuPopupHelper: MenuPopupHelper

    protected val dateFormatPattern: String by lazy { parentView.context.getString(R.string.diary_date_format) }

    protected var noteId: String? = null

    abstract fun bindTo(note: T?)

    protected fun initPopup(view: View) {
        val popup = PopupMenu(view.context, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.notes_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_note_edit -> {
                    noteId?.let { actionListener.onEdit(it) }
                    true
                }
                R.id.menu_note_delete -> {
                    noteId?.let { actionListener.onDelete(it) }
                    true
                }
                else -> false
            }
        }

        menuPopupHelper = MenuPopupHelper(view.context, popup.menu as MenuBuilder, view)
        menuPopupHelper.setForceShowIcon(true)
        menuPopupHelper.gravity = Gravity.END
    }

}