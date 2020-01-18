package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.presenters.PracticesPresenter
import com.allat.mboychenko.silverthread.presentation.views.listitems.PracticeItem
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import org.koin.android.ext.android.inject

class PracticesFragment: BaseAllatRaFragment(), IPracticesFragmentView {

    private var root: ConstraintLayout? = null
    private val presenter: PracticesPresenter by inject()

    private val constraintInit = ConstraintSet()
    private val constraintAutorep = ConstraintSet()
    private val constraintMed = ConstraintSet()
    private val constraintSpir = ConstraintSet()

    private val autoreportsSection = Section()
    private val meditationsSection = Section()
    private val spiritualSection = Section()

    private var viewState = PracticesViewState.INIT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_practices_init, container, false)
    }

    override fun toolbarTitle(): Int = R.string.practices

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addAnimationOperations()
        view.findViewById<TextView>(R.id.autoreports_category_desc).setOnClickListener(autoreportsOnClickListener)
        view.findViewById<TextView>(R.id.meditations_category_desc).setOnClickListener(meditationsOnClickListener)
        view.findViewById<TextView>(R.id.spirit_category_desc).setOnClickListener(spiritualOnClickListener)

        with(view.findViewById<RecyclerView>(R.id.autoreports_list)) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = com.xwray.groupie.GroupAdapter<ViewHolder>().apply {
                clear()
                add(autoreportsSection)
            }
        }
        with(view.findViewById<RecyclerView>(R.id.meditations_list)) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = com.xwray.groupie.GroupAdapter<ViewHolder>().apply {
                clear()
                add(meditationsSection)
            }
        }
        with(view.findViewById<RecyclerView>(R.id.spiritual_list)) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = com.xwray.groupie.GroupAdapter<ViewHolder>().apply {
                clear()
                add(spiritualSection)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detachView()
    }

    fun showInit(): Boolean {
        return if (viewState != PracticesViewState.INIT) {
            TransitionManager.beginDelayedTransition(root as ViewGroup)
            constraintInit.applyTo(root)
            viewState = PracticesViewState.INIT
            true
        } else {
            false
        }
    }

    private val autoreportsOnClickListener = View.OnClickListener {
        if (viewState != PracticesViewState.AUTOREPORTS) {
            TransitionManager.beginDelayedTransition(root as ViewGroup)
            constraintAutorep.applyTo(root)
            viewState = PracticesViewState.AUTOREPORTS
        }
    }

    private val meditationsOnClickListener = View.OnClickListener {
        if (viewState != PracticesViewState.MEDITATIONS) {
            TransitionManager.beginDelayedTransition(root as ViewGroup)
            constraintMed.applyTo(root)
            viewState = PracticesViewState.MEDITATIONS
        }
    }

    private val spiritualOnClickListener = View.OnClickListener {
        if (viewState != PracticesViewState.SPIRITUAL) {
            TransitionManager.beginDelayedTransition(root as ViewGroup)
            constraintSpir.applyTo(root)
            viewState = PracticesViewState.SPIRITUAL
        }
    }

    private fun addAnimationOperations() {
        root = view?.findViewById(R.id.root)

        constraintInit.clone(root)
        constraintAutorep.clone(context, R.layout.fragment_practices_autoreports)
        constraintMed.clone(context, R.layout.fragment_practices_meditations)
        constraintSpir.clone(context, R.layout.fragment_practices_spiritual)

        view?.findViewById<View>(R.id.autoreports)?.setOnClickListener(autoreportsOnClickListener)

        view?.findViewById<View>(R.id.meditations)?.setOnClickListener(meditationsOnClickListener)

        view?.findViewById<View>(R.id.spiritual)?.setOnClickListener(spiritualOnClickListener)
    }

    override fun onPracticesByCategoryReady(practices: List<PracticeItem>) {
        autoreportsSection.addAll( practices.filter { it.type == PracticeItem.PracticesType.AUTOREPORTS } )
        meditationsSection.addAll( practices.filter { it.type == PracticeItem.PracticesType.MEDITATIONS } )
        spiritualSection.addAll( practices.filter { it.type == PracticeItem.PracticesType.SPIRITUAL } )
    }

    override fun hasPractices(): Boolean =
        autoreportsSection.itemCount > 0 &&  meditationsSection.itemCount > 0 && spiritualSection.itemCount > 0

    override fun getFragmentTag(): String = MEDITATION_FRAGMENT_TAG

    enum class PracticesViewState {
        INIT,
        AUTOREPORTS,
        MEDITATIONS,
        SPIRITUAL
    }

    companion object {
        const val MEDITATION_FRAGMENT_TAG = "MEDITATION_FRAGMENT_TAG"
    }
}