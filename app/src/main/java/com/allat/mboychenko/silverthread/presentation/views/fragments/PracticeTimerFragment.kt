package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Group
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.models.PracticeStage
import com.allat.mboychenko.silverthread.presentation.presenters.PracticeTimerPresenter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.android.ext.android.inject

class PracticeTimerFragment : BaseAllatRaFragment(), IPracticeTimerFragmentView {

    private val presenter: PracticeTimerPresenter by inject()

    private lateinit var actionFab: FloatingActionButton
    private lateinit var allatPicker: NumberPicker
    private lateinit var minutesPicker: NumberPicker
    private lateinit var secondsPicker: NumberPicker
    private lateinit var stageName: TextView
    private lateinit var stageRemaning: TextView
    private lateinit var activeStageGroup: Group
    private lateinit var setupRemainingGroup: Group
    private lateinit var descDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        descDialog =  AlertDialog.Builder(context!!)
            .setTitle(R.string.description)
            .setMessage(R.string.practice_desk)
            .setCancelable(true)
            .setNegativeButton(R.string.hide) { dialog, _ -> dialog.dismiss()}
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.practice_timer_fragment, container, false)

        view.apply {
            actionFab = findViewById(R.id.action_fab)
            minutesPicker = findViewById(R.id.minutes)
            secondsPicker = findViewById(R.id.seconds)
            allatPicker = findViewById(R.id.allats)
            stageName = findViewById(R.id.stage)
            stageRemaning = findViewById(R.id.stageRemaining)
            activeStageGroup = findViewById(R.id.activeStageGroup)
            setupRemainingGroup = findViewById(R.id.setupRemainingGroup)

            allatPicker.minValue = 1
            allatPicker.maxValue = 12

            minutesPicker.minValue = 0
            minutesPicker.maxValue = 24
            secondsPicker.minValue = 0
            secondsPicker.maxValue = 59

            actionFab.setOnClickListener {
                stageRemaning.text = ""
                presenter.startStop()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        actionFab.setImageResource(R.drawable.ic_play)
        initStage()
        presenter.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detachView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chetverik_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.info -> {
                descDialog.show()
                true
            }
            else -> false
        }
    }

    override fun getFragmentTag() = PRACTICE_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.practice_timer

    override fun stageChanged(stage: PracticeStage, curAllat: Int) {
        when(stage) {
            PracticeStage.INIT -> initStage()
            PracticeStage.START -> activeStage(stage.getStageDesc(context!!))
            PracticeStage.ALLAT -> activeStage(stage.getStageDesc(context!!, curAllat))
        }
    }

    private fun initStage() {
        actionFab.setImageResource(R.drawable.ic_play)
        activeStageGroup.visibility = View.GONE
        setupRemainingGroup.visibility = View.VISIBLE
        stageRemaning.text = ""
    }

    private fun activeStage(value: String) {
        activeStageGroup.visibility = View.VISIBLE
        setupRemainingGroup.visibility = View.GONE
        stageName.text = value
        actionFab.setImageResource(R.drawable.ic_stop)
    }

    override fun getOffset(): Int {
        val mins = minutesPicker.value
        val sec = secondsPicker.value
        return sec + mins * 60
    }

    override fun getAllatsNum() = allatPicker.value

    override fun setMinsViewOffset(min: Int) {
        minutesPicker.value = min
    }

    override fun setSecondsViewOffset(sec: Int) {
        secondsPicker.value = sec
    }

    override fun stageTimeRemaining(mins: Int, sec: Int) {
        stageRemaning.text = String.format("%02d : %02d", mins, sec)
    }

    companion object {
        const val PRACTICE_FRAGMENT_TAG = "PRACTICE_FRAGMENT_TAG"
    }

}