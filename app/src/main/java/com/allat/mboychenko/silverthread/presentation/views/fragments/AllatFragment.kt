package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.presenters.AllatPresenter
import kotlinx.android.synthetic.main.allat_fragment.*
import org.koin.android.ext.android.inject

class AllatFragment: Fragment(), IAllatRaFragments, IAllatFragmentView {

    private val presenter: AllatPresenter by inject()

    override fun getFragmentTag(): String = ALLAT_FRAGMENT_TAG

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.allat_fragment, container, false)
    }

    override fun updateTimer(h: Long, m: Long, s: Long) {
        clockTextView.text = String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun updateTimerStatus(allatStatusTitle: String) {
        allatTitle.text = allatStatusTitle
    }

    override fun changeTimezoneSetupVisibility(visible: Boolean) {
        timezoneContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }
    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    companion object {
        const val ALLAT_FRAGMENT_TAG = "ALLAT_FRAGMENT_TAG"
    }
}
