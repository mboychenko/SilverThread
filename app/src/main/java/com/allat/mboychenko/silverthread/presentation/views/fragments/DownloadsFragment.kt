package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.bind
import com.allat.mboychenko.silverthread.presentation.presenters.DownloadsPresenter
import com.allat.mboychenko.silverthread.presentation.views.listitems.LoadedFileItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import org.koin.android.ext.android.inject

class DownloadsFragment : BaseAllatRaFragment(), IDownloadsFragmentView {

    private val downloadsList: RecyclerView by bind(R.id.downloadsList)
    private val noItemsGroup: Group by bind(R.id.noItemsGroup)
    private lateinit var groupAdapter: GroupAdapter<ViewHolder>

    private val filesSection = Section()

    private val presenter : DownloadsPresenter by inject()

    override fun getFragmentTag() = DOWNLOADS_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.downloads

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(downloadsList) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            groupAdapter = GroupAdapter<ViewHolder>().apply {
                add(filesSection)
            }
            adapter = groupAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        noFilesDescriptionVisibility(false)
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun filesList(files: List<LoadedFileItem>) {
        filesSection.update(files)
        noFilesDescriptionVisibility(filesSection.groupCount == 0)
    }

    override fun noFilesDescriptionVisibility(visible: Boolean) {
        if (visible) {
            noItemsGroup.visibility = View.VISIBLE
            downloadsList.visibility = View.GONE
        } else {
            noItemsGroup.visibility = View.GONE
            downloadsList.visibility = View.VISIBLE
        }
    }

    override fun removeLoadedItem(item: LoadedFileItem) {
        filesSection.remove(item)
        noFilesDescriptionVisibility(filesSection.groupCount == 0)
    }

    companion object {
        const val DOWNLOADS_FRAGMENT_TAG = "DOWNLOADS_FRAGMENT_TAG"
    }
}