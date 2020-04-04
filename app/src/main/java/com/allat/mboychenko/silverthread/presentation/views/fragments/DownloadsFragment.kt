package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.presenters.DownloadsPresenter
import com.allat.mboychenko.silverthread.presentation.views.listitems.LoadedFileItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import org.koin.android.ext.android.inject

class DownloadsFragment : BaseAllatRaFragment(), IDownloadsFragmentView {

    private lateinit var downloadsList: RecyclerView
    private lateinit var noPermissions: TextView
    private lateinit var noItemsGroup: Group
    private lateinit var groupAdapter: GroupAdapter<ViewHolder>

    private val filesSection = Section()

    private val presenter : DownloadsPresenter by inject()

    override fun getFragmentTag() = DOWNLOADS_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.downloads

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_downloads, container, false)
        downloadsList = view.findViewById(R.id.downloadsList)
        noItemsGroup = view.findViewById(R.id.noItemsGroup)
        noPermissions = view.findViewById(R.id.noPermissions)
        return view
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

        Handler().post { presenter.checkPermission() }
    }

    override fun onStart() {
        super.onStart()
        noFilesDescriptionVisibility(false)
        presenter.attachView(this)
    }

    override fun onStop() {
        super.onStop()
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
            noPermissions.visibility = View.GONE
        } else {
            noPermissions.visibility = View.GONE
            noItemsGroup.visibility = View.GONE
            downloadsList.visibility = View.VISIBLE
        }
    }

    override fun showNoPermissions() {
        noPermissions.visibility = View.VISIBLE
    }

    override fun removeLoadedItem(item: LoadedFileItem) {
        filesSection.remove(item)
        noFilesDescriptionVisibility(filesSection.groupCount == 0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for ((position, permission) in permissions.withIndex()) {
                val grantResult = grantResults[position]
                if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Handler().post { presenter.checkFilesList() }
                    }
                }
            }
        }
    }

    override fun requestStoragePermission() {
        try {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e("Permission request fail", e.message ?: "nothing to explain")
        }
    }

    companion object {
        const val DOWNLOADS_FRAGMENT_TAG = "DOWNLOADS_FRAGMENT_TAG"
        const val PERMISSION_REQUEST_CODE = 342
    }
}