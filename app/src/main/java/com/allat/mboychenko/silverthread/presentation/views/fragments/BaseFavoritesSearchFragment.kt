package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.presenters.ListFavoritePresenter
import com.allat.mboychenko.silverthread.presentation.presenters.ListFavoritePresenter.ListState
import com.allat.mboychenko.silverthread.presentation.views.listitems.QuoteItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class BaseFavoritesSearchFragment : BaseAllatRaFragment(), IListFavFragmentView, SearchView.OnQueryTextListener {

    private lateinit var noItemsToShow: TextView

    private var subscription: Disposable? = null
    private val searchObserver = PublishSubject.create<String>()

    private val listSection = Section()

    protected abstract fun getPresenter(): ListFavoritePresenter<IListFavFragmentView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getInt(SAVE_FAVORITE_STATE_KEY, -1)?.let {
            if (it != -1) {
                getPresenter().changeListFavState(ListState.values()[it])
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_FAVORITE_STATE_KEY, getPresenter().getListFavState().ordinal)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscription = searchObserver
            .skip(1)
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.computation())
            .map { getPresenter().filterVisibleQuotes(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { updateList(it) }
            .subscribe()
    }

    protected fun initList(listView: RecyclerView) {
        with(listView) {
            layoutManager = LinearLayoutManager(context)
            adapter = GroupAdapter<ViewHolder>().apply {
                add(listSection)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listSection.setHideWhenEmpty(true)
        noItemsToShow = view.findViewById(R.id.noItemsToShow)
    }

    override fun onStart() {
        super.onStart()
        getPresenter().attachView(this)

        if (listSection.itemCount == 0) {
            getPresenter().loadItemsByState()
        }
    }

    override fun onStop() {
        super.onStop()
        getPresenter().detachView()
    }

    override fun updateList(items: List<QuoteItem>) {
        noItemsToShow.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        listSection.update(items)
    }

    override fun removeFavItem(item: QuoteItem) {
        listSection.remove(item)
        if (listSection.itemCount == 0) {
            noItemsToShow.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        subscription?.let {
            it.dispose()
            subscription = null
        }

        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.favorites_menu_item, menu)
        updateFavMenuItemIcon(menu.findItem(R.id.favorites))

        val favItem = menu.findItem(R.id.favorites)
        val searchItem = menu.findItem(R.id.search)

        (searchItem.actionView as SearchView).apply {
            setOnQueryTextListener(this@BaseFavoritesSearchFragment)
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                favItem.isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                activity?.invalidateOptionsMenu()
                return true

            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.favorites -> {
                getPresenter().changeListFavState()
                updateFavMenuItemIcon(item)
                true
            }
            else -> false
        }
    }

    private fun updateFavMenuItemIcon(item: MenuItem) {
        context?.let {
            if (getPresenter().getListFavState() == ListState.FAVORITE) {
                item.icon = ContextCompat.getDrawable(it, R.drawable.ic_favorite_black_24dp)
            } else {
                item.icon = ContextCompat.getDrawable(it, R.drawable.ic_favorite_border_black_24dp)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { searchObserver.onNext(it) }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { searchObserver.onNext(it) }
        return true
    }

    companion object {
        const val SAVE_FAVORITE_STATE_KEY = "SAVE_FAVORITE_STATE_KEY"
    }

}