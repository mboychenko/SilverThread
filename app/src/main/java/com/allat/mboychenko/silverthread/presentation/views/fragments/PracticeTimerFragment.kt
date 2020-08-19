package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.nfc.Tag
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.models.PracticeStage
import com.allat.mboychenko.silverthread.presentation.presenters.PracticeTimerPresenter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.practice_editor_view.*
import kotlinx.android.synthetic.main.practice_timer_fragment.*
import org.koin.android.ext.android.inject

class PracticeTimerFragment : BaseAllatRaFragment(), IPracticeTimerFragmentView {

    private val presenter: PracticeTimerPresenter by inject()

    private lateinit var actionFab: FloatingActionButton
    private lateinit var allatPicker: NumberPicker
    private lateinit var minutesPicker: NumberPicker


    private lateinit var secondsPicker: NumberPicker
    private lateinit var stageName: TextView
    private lateinit var stageRemaning: TextView
    private lateinit var allatLengthSwitch: SwitchCompat
    private lateinit var volumeHigh: SwitchCompat
    private lateinit var volumeLabel : TextView

    private lateinit var halfAllatText: TextView
    private lateinit var fullAllatText: TextView
    private lateinit var activeStageGroup: Group
    private lateinit var setupRemainingGroup: Group
    private lateinit var descDialog: AlertDialog

    private val textColorActive by lazy { ContextCompat.getColor(context!!, R.color.colorActive) }
    private val textColorDefault by lazy { halfAllatText.textColors }


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
            allatLengthSwitch = findViewById(R.id.switchAllat)
            halfAllatText = findViewById(R.id.shortAllat)
            fullAllatText = findViewById(R.id.fullAllat)
            volumeLabel = findViewById(R.id.volumeLabel)
            volumeHigh = findViewById(R.id.volumeChange)

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

    override fun setAllatLengthShort(short: Boolean) {
        allatLengthSwitch.isChecked = short.not()
        switchTextColorUpdate(short.not())

        allatLengthSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.setAllatLengthShort(!isChecked)
            switchTextColorUpdate(isChecked)
        }
    }

    override fun getAllatLengthShort(): Boolean {
        return allatLengthSwitch.isChecked.not()
    }


    override fun setVolumeHigh(short: Boolean) {
        volumeHigh.isChecked = short.not()

        volumeHigh.setOnCheckedChangeListener { _, isChecked ->
            presenter.setVolumeHigh(!isChecked)
        }
    }

    override fun getVolumeHigh(): Boolean {
        return volumeHigh.isChecked.not()
    }

    private fun switchTextColorUpdate(isChecked: Boolean) =
        if (isChecked) {
            fullAllatText.setTextColor(textColorActive)
            halfAllatText.setTextColor(textColorDefault)
        } else {
            fullAllatText.setTextColor(textColorDefault)
            halfAllatText.setTextColor(textColorActive)
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