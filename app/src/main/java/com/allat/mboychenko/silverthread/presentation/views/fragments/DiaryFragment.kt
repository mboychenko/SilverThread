package com.allat.mboychenko.silverthread.presentation.views.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryNoteEditorDialog
import com.allat.mboychenko.silverthread.presentation.views.dialogs.DiaryPracticeEditorNoteDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class DiaryFragment : BaseAllatRaFragment() {

    override fun getFragmentTag() = NOTES_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.notes

    private lateinit var pager: ViewPager
    private lateinit var tabs: TabLayout
    private lateinit var addFab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.notes_fragment, container, false)
        pager = view.findViewById(R.id.pager)
        tabs = view.findViewById(R.id.tabs)
        addFab = view.findViewById(R.id.add_fab)
        pager.adapter = ViewPagerAdapter(childFragmentManager, context!!)
        tabs.setupWithViewPager(pager)
        return view
    }

    override fun onStart() {
        super.onStart()
        tabs.addOnTabSelectedListener(tabSelectionListener)
        updateFabAction(tabs.selectedTabPosition)
    }

    override fun onStop() {
        super.onStop()
        tabs.removeOnTabSelectedListener(tabSelectionListener)
    }

    private val tabSelectionListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let { updateFabAction(it.position) }
        }
    }

    private fun updateFabAction(position: Int) {
        addFab.setOnClickListener {
            if (position == POSITION_PRACTICES) {
                DiaryPracticeEditorNoteDialog.getInstance()
                    .show(childFragmentManager,
                        DiaryPracticeEditorNoteDialog.DIARY_PRACTICE_EDITOR_DIALOG_TAG
                    )
            } else if (position == POSITION_DIARY) {
                DiaryNoteEditorDialog().show(
                    childFragmentManager,
                    DiaryNoteEditorDialog.DIARY_NOTE_EDITOR_DIALOG_TAG
                )
            }
        }
    }

    class ViewPagerAdapter(fm: FragmentManager, val context: Context) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val diaryNotesTitle: String by lazy { context.getString(R.string.diary) }
        private val practicesTitle: String by lazy { context.getString(R.string.practices) }

        override fun getItem(position: Int): Fragment {
            return when(position) {
                POSITION_PRACTICES -> DiaryPracticesFragment()
                else -> DiaryNotesFragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                POSITION_PRACTICES -> practicesTitle
                else -> diaryNotesTitle
            }
        }

        override fun getCount() = 2

    }

    companion object {
        const val NOTES_FRAGMENT_TAG = "NOTES_FRAGMENT_TAG"
        private const val POSITION_PRACTICES = 0
        private const val POSITION_DIARY = 1
    }
}