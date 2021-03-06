package com.allat.mboychenko.silverthread.presentation.views.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_ACTION_CHETVERIK
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_ACTION_QUOTE
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_ACTION_RADIO
import com.allat.mboychenko.silverthread.presentation.helpers.NOTIFICATION_QUOTE_POSITION_EXTRAS
import com.allat.mboychenko.silverthread.presentation.views.fragments.*
import com.allat.mboychenko.silverthread.presentation.views.fragments.webview.AllatRaWebViewURIConstants
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView


abstract class BaseNavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var appBar: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())
        initViews()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            NOTIFICATION_ACTION_QUOTE -> {
                setQuotesNavigationItem(intent)
                return
            }
            NOTIFICATION_ACTION_RADIO -> {
                setRadioNavigationItem()
                return
            }
            NOTIFICATION_ACTION_CHETVERIK -> {
                setPracticeTimerNavigationItem()
                return
            }
        }
    }

    abstract fun getContentView(): Int

    @CallSuper
    open fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        drawer = findViewById(R.id.drawer)
        navigationView = findViewById(R.id.navigationView)
        appBar = findViewById(R.id.appBar)

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

    fun getToolbar(): Toolbar = toolbar

    fun getAppBar(): AppBarLayout = appBar

    fun getDrawer(): DrawerLayout = drawer

    protected fun setFragmentByNavId(navId: Int, updateNavItem: Boolean = false) {
        when (navId) {
            R.id.nav_allat ->
                setFragment(AllatFragment(), navId)
            R.id.nav_practices ->
                setFragment(PracticesFragment(), navId)
            R.id.nav_practice_timer ->
                setFragment(PracticeTimerFragment(), navId)
            R.id.nav_quotes ->
                setFragment(QuotesFragment(), navId)
            R.id.nav_parables ->
                setFragment(ParablesFragment(), navId)
            R.id.nav_books ->
                setFragment(BooksFragment(), navId)
            R.id.nav_radio ->
                setFragment(RadioFragment(), navId)
            R.id.nav_downloads ->
                setFragment(DownloadsFragment(), navId)
            R.id.nav_notes ->
                setFragment(DiaryFragment(), navId)
            R.id.nav_backup ->
                setFragment(BackupFragment(), navId)
            R.id.nav_settings ->
                setFragment(SettingsFragment(), navId)
            R.id.nav_tv_im ->
                webViewLink(AllatRaWebViewURIConstants.URI_ALLATRA_TV_IM)
            R.id.nav_tv ->
                webViewLink(AllatRaWebViewURIConstants.URI_ALLATRA_TV)
            R.id.nav_znai ->
                webViewLink(AllatRaWebViewURIConstants.URI_ZNAI)
            R.id.nav_org ->
                webViewLink(AllatRaWebViewURIConstants.URI_ALLATRA_ORG)
            R.id.nav_unites ->
                webViewLink(AllatRaWebViewURIConstants.URI_ALLATRAUNITES)
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
                PracticesFragment.MEDITATION_FRAGMENT_TAG -> R.id.nav_practices
                RadioFragment.RADIO_FRAGMENT_TAG -> R.id.nav_radio
                PracticeTimerFragment.PRACTICE_FRAGMENT_TAG -> R.id.nav_practice_timer
                QuotesFragment.QUOTES_FRAGMENT_TAG -> R.id.nav_quotes
                ParablesFragment.PARABLES_FRAGMENT_TAG -> R.id.nav_parables
                BooksFragment.BOOKS_FRAGMENT_TAG -> R.id.nav_books
                DownloadsFragment.DOWNLOADS_FRAGMENT_TAG -> R.id.nav_downloads
                DiaryFragment.NOTES_FRAGMENT_TAG -> R.id.nav_notes
                BackupFragment.BACKUP_FRAGMENT_TAG -> R.id.nav_backup
                SettingsFragment.SETTINGS_FRAGMENT_TAG -> R.id.nav_settings
                else -> R.id.nav_allat
            }
        } else if (webViewUrl != null) {
            navId = when {
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_ALLATRA_TV_IM) -> R.id.nav_tv_im
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_ALLATRA_TV) -> R.id.nav_tv
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_ZNAI) -> R.id.nav_znai
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_ALLATRA_ORG) ||
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_ALLATRA_ORG_UNI_GRAIN)-> R.id.nav_org
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_ALLATRAUNITES) -> R.id.nav_unites
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_VESTI) -> R.id.nav_vesti
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_SCIENCE) -> R.id.nav_science
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_PARTNER) -> R.id.nav_partner
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_CRAUD) -> R.id.nav_craud
                webViewUrl.startsWith(AllatRaWebViewURIConstants.URI_GEO) -> R.id.nav_geo
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

//        supportFragmentManager.popBackStack(fragmentTag, POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, fragmentTag)
//            .addToBackStack(fragmentTag)
            .addToBackStack(null)
            .commit()
    }

    protected fun navigationItemPosUpdate() {
        supportFragmentManager.fragments.find { it.isVisible || it.isAdded }?.let {
            if (it is IAllatRaFragments) {
                updateNavDrawerBy(fragmentTag = (it as IAllatRaFragments).getFragmentTag())
            }
            return
        }

        when (intent.action) {
            NOTIFICATION_ACTION_QUOTE -> setQuotesNavigationItem(intent)
            NOTIFICATION_ACTION_RADIO -> setRadioNavigationItem()
            NOTIFICATION_ACTION_CHETVERIK -> setPracticeTimerNavigationItem()
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

    private fun setPracticeTimerNavigationItem() {
        navigationView.setCheckedItem(R.id.nav_practice_timer)
        setFragment(PracticeTimerFragment())
    }

    private fun setQuotesNavigationItem(intent: Intent?) {
        intent?.let {
            val position = it.getIntExtra(NOTIFICATION_QUOTE_POSITION_EXTRAS, -1)
            if (position != -1) {
                it.removeExtra(NOTIFICATION_QUOTE_POSITION_EXTRAS)

                navigationView.setCheckedItem(R.id.nav_quotes)

                if (supportFragmentManager.findFragmentByTag(QuotesFragment.QUOTES_FRAGMENT_TAG)?.isVisible == true) {
                    (supportFragmentManager.findFragmentByTag(QuotesFragment.QUOTES_FRAGMENT_TAG) as QuotesFragment)
                        .showIncomingFromNotificationQuote(position)
                } else {
                    setFragment(QuotesFragment.newInstance(position))
                }
            }
        }
    }

    private var exitConfirmation = false
    private val exitCountDown = object: CountDownTimer(1500, 500) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            exitConfirmation = false
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (supportFragmentManager.fragments.size > 0) {

                val manageBackFragment =
                    supportFragmentManager.findFragmentByTag(PracticesFragment.MEDITATION_FRAGMENT_TAG) ?:
                    supportFragmentManager.findFragmentByTag(BackupFragment.BACKUP_FRAGMENT_TAG)
                if (manageBackFragment is IManageBackNavFragment &&
                    manageBackFragment.isVisible &&
                    manageBackFragment.showInit()) {
                    return
                }

                if (supportFragmentManager.backStackEntryCount <= 1 &&
                    supportFragmentManager.fragments[0].tag == AllatFragment.ALLAT_FRAGMENT_TAG) {

                    if (!exitConfirmation) {
                        exitCountDown.start()
                        exitConfirmation = true
                        Toast.makeText(this, getString(R.string.press_double_to_exit), Toast.LENGTH_SHORT).show()
                    } else {
                        finish()
                    }

                    return
                } else if (supportFragmentManager.backStackEntryCount <= 1 &&
                    supportFragmentManager.fragments[0].tag != AllatFragment.ALLAT_FRAGMENT_TAG) {
                    supportFragmentManager.popBackStack()
                    setFragment(AllatFragment())
                    updateNavItemInHandlerQueue()
                    return
                } else {
                    supportFragmentManager.popBackStack()
                    updateNavItemInHandlerQueue()
                    return
                }
            }

            super.onBackPressed()
            updateNavItemInHandlerQueue()
        }
    }

    private fun updateNavItemInHandlerQueue() = Handler().post { navigationItemPosUpdate() }

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