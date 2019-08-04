package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.allat.mboychenko.silverthread.presentation.helpers.bind
import com.allat.mboychenko.silverthread.presentation.helpers.px
import com.allat.mboychenko.silverthread.presentation.presenters.RadioPresenter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.android.ext.android.inject


class RadioFragment : Fragment(), IAllatRaFragments, IRadioFragmentView {

    private val presenter: RadioPresenter by inject()

    private lateinit var progressView: View
    private lateinit var onAirStatus: AppCompatTextView
    private lateinit var onlineStatus: AppCompatTextView
    private lateinit var stopFab: FloatingActionButton
    private lateinit var playFab: FloatingActionButton
//    private lateinit var pauseFab: FloatingActionButton
    private val pauseFab: FloatingActionButton by bind(R.id.pauseFab)

    private var currentPlayerButtonsState = PlayerButtonsState.INIT

    override fun getCurrentPlayerButtonsState() = currentPlayerButtonsState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_radio, container, false)

        playFab = view.findViewById(R.id.playFab)
        playFab.setOnClickListener {
            if (presenter.noInternet && (currentPlayerButtonsState == PlayerButtonsState.IDLE ||
                        currentPlayerButtonsState == PlayerButtonsState.INIT)) {
                presenter.checkOnline()
                Snackbar.make(view, getString(R.string.no_internet), Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(context!!, android.R.color.holo_red_light))
                    .show()
            } else {
                playButtonState()
                presenter.play()
            }
        }

//        pauseFab = view.findViewById(R.id.pauseFab)
        pauseFab.setOnClickListener {
            pauseButtonState()
            presenter.pause()
        }

        stopFab = view.findViewById(R.id.stopFab)
        stopFab.setOnClickListener {
            stopButtonState()
            presenter.stop()
        }

        progressView = view.findViewById<View>(R.id.progressView)
        onAirStatus = view.findViewById(R.id.onAirStatus)
        onlineStatus = view.findViewById(R.id.onlineStatus)
        return view
    }

    override fun stopButtonState() {
        currentPlayerButtonsState = PlayerButtonsState.INIT
        stopFab.animateFabShowHide(false)
        pauseFab.visibility = View.INVISIBLE
        playFab.visibility = View.VISIBLE
    }

    override fun pauseButtonState() {
        currentPlayerButtonsState = PlayerButtonsState.PAUSED
        playFab.visibility = View.VISIBLE
        pauseFab.visibility = View.INVISIBLE
    }

    override fun playButtonState() {
        currentPlayerButtonsState = PlayerButtonsState.PLAYING
        playFab.visibility = View.GONE
        pauseFab.visibility = View.VISIBLE
        if (stopFab.visibility != View.VISIBLE) {
            stopFab.animateFabShowHide(true)
        }
    }

    override fun playButtonsIdleState() {
        currentPlayerButtonsState = PlayerButtonsState.IDLE
        playFab.visibility = View.VISIBLE
        pauseFab.visibility = View.INVISIBLE
        if (stopFab.visibility != View.VISIBLE) {
            stopFab.animateFabShowHide(true)
        }
    }

    override fun onResume() {
        super.onResume()
        showProgress()
        presenter.attachView(this)
    }

    override fun showProgress() {
        progressView.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressView.visibility = View.GONE
    }

    override fun updateOnlineStatus(online: Boolean) {
        if (online) {
            onlineStatus.text = getString(R.string.online)
            val activeColor = ContextCompat.getColor(context!!, R.color.green)
            onlineStatus.setTextColor(activeColor)
            onlineStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(context!!, R.drawable.ic_online), null, null, null)
        } else {
            onlineStatus.text = getString(R.string.offline)
            onlineStatus.setTextColor(ContextCompat.getColor(context!!, R.color.red))
            onlineStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(context!!, R.drawable.ic_offline), null, null, null)
        }
    }

    override fun updateOnAirStatus(colorId: Int, textId: Int) {
        context?.let {
            val drawableIndicator = AppCompatResources.getDrawable(context!!, R.drawable.ic_offline)
            val middleColoredDrawable = DrawableCompat.wrap(drawableIndicator!!.mutate())
            DrawableCompat.setTint(middleColoredDrawable, ContextCompat.getColor(context!!, colorId))

            onAirStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(middleColoredDrawable, null, null, null)
            onAirStatus.setTextColor(ContextCompat.getColor(it, colorId))
            onAirStatus.text = it.getString(textId)
        }
    }

    private fun FloatingActionButton.animateFabShowHide(show: Boolean) {
        val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
        val animRadius: ValueAnimator
        if (show) {
            animRadius = ValueAnimator.ofInt(0, 80.px)
            animRadius.doOnStart { visibility = View.VISIBLE }
        } else {
            animRadius = ValueAnimator.ofInt(80.px, 0)
            animRadius.doOnEnd { visibility = View.INVISIBLE }
        }

        animRadius.addUpdateListener { valueAnimator ->
            val margin = valueAnimator.animatedValue as Int
            marginLayoutParams.marginEnd = margin
            layoutParams = marginLayoutParams
        }

        animRadius.duration = 200
        animRadius.interpolator = LinearInterpolator()
        animRadius.start()
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun getFragmentTag(): String = RADIO_FRAGMENT_TAG

    enum class PlayerButtonsState {
        PLAYING,
        PAUSED,
        INIT,
        IDLE
    }

    companion object {
        const val RADIO_FRAGMENT_TAG = "RADIO_FRAGMENT_TAG"
    }

}