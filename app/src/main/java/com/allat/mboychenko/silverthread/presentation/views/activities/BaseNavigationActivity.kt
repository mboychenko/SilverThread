package com.allat.mboychenko.silverthread.presentation.views.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_ACTION_QUOTE
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_ACTION_RADIO
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_QUOTE_POSITION_EXTRAS
import com.allat.mboychenko.silverthread.presentation.views.fragments.*
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


abstract class BaseNavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())
        initViews()
    }

    abstract fun getContentView(): Int

    @CallSuper
    open fun initViews() {
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

    }

    protected abstract fun webViewLink(uri: String)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setFragmentByNavId(item.itemId)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun getToolbar() = toolbar

    protected fun setFragmentByNavId(navId: Int, updateNavItem: Boolean = false) {
        when (navId) {
            R.id.nav_allat ->
                setFragment(AllatFragment(), navId)
            R.id.nav_meditations ->
                setFragment(MeditationsFragment(), navId)
            R.id.nav_quotes ->
                setFragment(QuotesFragment(), navId)
            R.id.nav_books ->
                setFragment(BooksFragment(), navId)
            R.id.nav_radio ->
                setFragment(RadioFragment(), navId)
            R.id.nav_about_us ->
                setFragment(AboutFragment(), navId)
            R.id.nav_tv ->
                webViewLink(AllatRaWebViewURIConstants.URI_ALLATRA_TV)
            R.id.nav_znai ->
                webViewLink(AllatRaWebViewURIConstants.URI_ZNAI)
            R.id.nav_org ->
                webViewLink(AllatRaWebViewURIConstants.URI_ALLATRA_ORG)
            R.id.nav_vesti ->
                webViewLink(AllatRaWebViewURIConstants.URI_VESTI)
            R.id.nav_science ->
                webViewLink(AllatRaWebViewURIConstants.URI_SCIENCE)
            R.id.nav_partner ->
                webViewLink(AllatRaWebViewURIConstants.URI_PARTNER)
            R.id.nav_craud ->
                webViewLink(AllatRaWebViewURIConstants.URI_CRAUD)
            R.id.nav_geo ->
                webViewLink(AllatRaWebViewURIConstants.URI_GEO)
        }

        if (updateNavItem) {
            navigationView.setCheckedItem(navId)
        }
    }

    protected fun updateNavDrawerBy(fragmentTag: String? = null, webViewUrl: String? = null) {
        var navId = R.id.nav_allat
        if (fragmentTag != null) {
            navId = when (fragmentTag) {
                AllatFragment.ALLAT_FRAGMENT_TAG -> R.id.nav_allat
                MeditationsFragment.MEDITATION_FRAGMENT_TAG -> R.id.nav_meditations
                RadioFragment.RADIO_FRAGMENT_TAG -> R.id.nav_radio
                QuotesFragment.QUOTES_FRAGMENT_TAG -> R.id.nav_quotes
                BooksFragment.BOOKS_FRAGMENT_TAG -> R.id.nav_books
                AboutFragment.ABOUT_FRAGMENT_TAG -> R.id.nav_about_us
                else -> R.id.nav_allat
            }
        } else if (webViewUrl != null) {
            navId = when (webViewUrl) {
                AllatRaWebViewURIConstants.URI_ALLATRA_TV -> R.id.nav_tv
                AllatRaWebViewURIConstants.URI_ZNAI -> R.id.nav_znai
                AllatRaWebViewURIConstants.URI_ALLATRA_ORG -> R.id.nav_org
                AllatRaWebViewURIConstants.URI_VESTI -> R.id.nav_vesti
                AllatRaWebViewURIConstants.URI_SCIENCE -> R.id.nav_science
                AllatRaWebViewURIConstants.URI_PARTNER -> R.id.nav_partner
                AllatRaWebViewURIConstants.URI_CRAUD -> R.id.nav_craud
                AllatRaWebViewURIConstants.URI_GEO -> R.id.nav_geo
                else -> R.id.nav_tv
            }
        }
        navigationView.setCheckedItem(navId)
    }

    protected open fun setFragment(fragment: Fragment, navId: Int = -1) {
        val fragmentTag = (fragment as IAllatRaFragments).getFragmentTag()

        if (supportFragmentManager.findFragmentByTag(fragmentTag)?.isVisible == true) {
            return
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, fragmentTag)
            .addToBackStack(null)
            .commit()
    }

    protected fun navigationItemPosUpdate() {
        supportFragmentManager.fragments.find { it.isVisible || it.isAdded }?.let {
            updateNavDrawerBy(fragmentTag = (it as IAllatRaFragments).getFragmentTag())
            return
        }

        when (intent.action) {
            NOTIFICATION_ACTION_QUOTE -> setQuotesNavigationItem()
            NOTIFICATION_ACTION_RADIO -> setRadioNavigationItem()
            else -> setDefaultNavigationItem()
        }

    }

    private fun setDefaultNavigationItem() {
        navigationView.setCheckedItem(R.id.nav_allat)
        setFragment(AllatFragment())
    }

    private fun setRadioNavigationItem() {
        navigationView.setCheckedItem(R.id.nav_radio)
        setFragment(RadioFragment())
    }

    private fun setQuotesNavigationItem() {
        val position = intent.getIntExtra(NOTIFICATION_QUOTE_POSITION_EXTRAS, -1)
        navigationView.setCheckedItem(R.id.nav_quotes)
        setFragment(QuotesFragment.newInstance(position))
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            navigationItemPosUpdate()
        }
    }

    protected fun turnOffToolbarScrolling() {
        val toolbarLayoutParams = toolbar.layoutParams as AppBarLayout.LayoutParams
        toolbarLayoutParams.scrollFlags = 0
        toolbar.layoutParams = toolbarLayoutParams

        val appBarLayoutParams = appBar.layoutParams as CoordinatorLayout.LayoutParams
        appBarLayoutParams.behavior = null
        appBar.layoutParams = appBarLayoutParams
    }

    protected fun turnOnToolbarScrolling() {
        val toolbarLayoutParams = toolbar.layoutParams as AppBarLayout.LayoutParams
        toolbarLayoutParams.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        toolbar.layoutParams = toolbarLayoutParams

        val appBarLayoutParams = appBar.layoutParams as CoordinatorLayout.LayoutParams
        appBarLayoutParams.behavior = AppBarLayout.Behavior()
        appBar.layoutParams = appBarLayoutParams
    }

}