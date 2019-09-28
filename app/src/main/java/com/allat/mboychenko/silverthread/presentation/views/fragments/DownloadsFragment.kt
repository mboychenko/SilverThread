package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    private lateinit var groupAdapter: GroupAdapter<ViewHolder>
    private val noFiles: TextView by bind(R.id.noFiles)

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
        noFiles.visibility = View.GONE
        presenter.attachView(this)

    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun filesList(files: List<LoadedFileItem>) {
        filesSection.update(files)
        if (filesSection.groupCount > 0) {
            noFiles.visibility = View.GONE
        }
    }

    override fun noFilesInDirectory() {
        noFiles.visibility = View.VISIBLE
    }

    override fun removeLoadedItem(item: LoadedFileItem) {
        filesSection.remove(item)
        if (filesSection.groupCount == 0) {
            noFilesInDirectory()
        }
    }

    companion object {
        const val DOWNLOADS_FRAGMENT_TAG = "DOWNLOADS_FRAGMENT_TAG"
    }
}