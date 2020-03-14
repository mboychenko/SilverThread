package com.allat.mboychenko.silverthread.presentation.views.adapters

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.allat.mboychenko.silverthread.presentation.models.ItemIdentifiable
import com.allat.mboychenko.silverthread.presentation.views.viewholders.DiaryBaseViewHolder

abstract class BaseDiaryAdapter<T: ItemIdentifiable, VH : DiaryBaseViewHolder<T>> :
    PagedListAdapter<T, VH>(diffCallback as DiffUtil.ItemCallback<T>) {

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = getItem(position)
        holder.bindTo(note)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ItemIdentifiable>() {
            override fun areItemsTheSame(
                oldItem: ItemIdentifiable,
                newItem: ItemIdentifiable
            ): Boolean =
                oldItem.id() == newItem.id()

            override fun areContentsTheSame(
                oldItem: ItemIdentifiable,
                newItem: ItemIdentifiable
            ): Boolean =
                oldItem == newItem
        }
    }

}