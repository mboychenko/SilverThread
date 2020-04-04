package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.*
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.presenters.ListFavoritePresenter
import com.allat.mboychenko.silverthread.presentation.presenters.ParablesPresenter
import kotlinx.android.synthetic.main.fragment_parables_list.view.*
import org.koin.android.ext.android.inject

class ParablesFragment : BaseFavoritesSearchFragment() {

    private val presenter: ParablesPresenter by inject()

    override fun getPresenter(): ListFavoritePresenter<IListFavFragmentView> = presenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parables_list, container, false)

        initList(view.parablesList)

        return view
    }

    override fun getFragmentTag() = PARABLES_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.parables_title

    companion object {
        const val PARABLES_FRAGMENT_TAG = "PARABLES_FRAGMENT_TAG"
    }
}