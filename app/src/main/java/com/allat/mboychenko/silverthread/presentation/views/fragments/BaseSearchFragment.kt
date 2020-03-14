package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

abstract class BaseSearchFragment : Fragment(), SearchView.OnQueryTextListener {
    private var subscription: Disposable? = null
    private val searchObserver = PublishSubject.create<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscription = searchObserver
            .skip(1)
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { search(it) }
            .subscribe()
    }

    override fun onDestroyView() {
        subscription?.let {
            it.dispose()
            subscription = null
        }

        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_menu, menu)
        val calendarItem = menu.findItem(R.id.calendar)
        val searchItem = menu.findItem(R.id.search)

        (searchItem.actionView as SearchView).apply {
            setOnQueryTextListener(this@BaseSearchFragment)
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                calendarItem.isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                activity?.invalidateOptionsMenu()
                return true

            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }



    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { searchObserver.onNext(it) }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { searchObserver.onNext(it) }
        return true
    }

    protected abstract fun search(text: String)

    protected fun datesFilterRangeToString(dates: Pair<Calendar, Calendar?>): String {
        val (start, end) = dates
        return if (end != null) {
            String.format("%s - %s", DateFormat.format(DATE_FORMAT_PATTERN, start), DateFormat.format(DATE_FORMAT_PATTERN, end))
        } else {
            DateFormat.format(DATE_FORMAT_PATTERN, start).toString()
        }
    }

    companion object {
        private const val DATE_FORMAT_PATTERN = "dd MMM, yyyy"
    }
}