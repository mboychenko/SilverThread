package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.OptimizationUtils

class SettingsFragment : BaseAllatRaFragment() {

    companion object {
        const val SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT_TAG"
    }

    override fun getFragmentTag() = SETTINGS_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.settings_toolbar_title

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        view.findViewById<Button>(R.id.autostart).setOnClickListener {
            context?.let {
                val found = OptimizationUtils.autoStartManager(it)
                if(!found) {
                    Toast.makeText(it, R.string.autostart_mng_not_found, Toast.LENGTH_LONG).show()
                }
            }
        }
        view.findViewById<Button>(R.id.pm).setOnClickListener {
            context?.let {
                val found = OptimizationUtils.powerManager(it)
                if(!found) {
                    Toast.makeText(it, R.string.power_mng_not_found, Toast.LENGTH_LONG).show()
                }
            }
        }
        return view
    }


}