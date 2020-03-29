package com.allat.mboychenko.silverthread.presentation.views.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.capitalizeEachNewWord

class PracticeFilterSpinner(context: Context, attrs: AttributeSet) : AppCompatSpinner(context, attrs) {

    private val firstAllRaw: String by lazy { context.getString(R.string.all) }

    private var adapter: ArrayAdapter<String>? = null

    private var selectionListener: ((String?) -> Unit)? = null

    fun setSelectionCallback(listener: (String?) -> Unit) {
        selectionListener = listener
    }

    fun setAdapter(adapter: ArrayAdapter<String>) {
        this.adapter = adapter
        super.setAdapter(adapter)
    }

    private val practiceFilterSelectionListener = object : OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            selectionListener?.invoke(if (position == 0 || adapter == null) null else adapter!!.getItem(position))
        }

    }

    private val currentList = mutableListOf<String>()

    fun placeHolder() {
        if (adapter != null && count == 0) {
            onItemSelectedListener = null
            adapter!!.add(firstAllRaw)
            onItemSelectedListener = practiceFilterSelectionListener
        }
    }

    fun updateItems(items: List<String>, restoreSelection: String? = null) {
        if (adapter != null) {
            if (items.isEmpty()) {
                currentList.clear()
                adapter!!.clear()
                placeHolder()
            } else if (!currentList.toTypedArray().contentEquals(items.toTypedArray())) {
                onItemSelectedListener = null

                val formatted = items.map { it.capitalizeEachNewWord() }

                adapter!!.clear()
                adapter!!.addAll(listOf(firstAllRaw) + formatted)

                currentList.clear()
                currentList.addAll(items)

                setSelection(formatted.indexOf(restoreSelection) + 1)

                onItemSelectedListener = practiceFilterSelectionListener
            } else if (adapter!!.getPosition(restoreSelection) != selectedItemPosition){
                val pos = if (restoreSelection != null) adapter!!.getPosition(restoreSelection) else 0
                setSelection(pos)
            }
        }
    }

}