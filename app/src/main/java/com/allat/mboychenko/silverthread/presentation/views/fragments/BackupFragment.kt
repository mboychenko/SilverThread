package com.allat.mboychenko.silverthread.presentation.views.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatSpinner
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.allat.mboychenko.silverthread.R
import com.allat.mboychenko.silverthread.presentation.helpers.BACKUP_FOLDER_NAME
import com.allat.mboychenko.silverthread.presentation.intents.BackupAction.*
import com.allat.mboychenko.silverthread.presentation.viewmodels.BackupViewModel
import com.allat.mboychenko.silverthread.presentation.views.custom.PasswordView
import com.allat.mboychenko.silverthread.presentation.viewstate.BackupViewState
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.properties.Delegates

class  BackupFragment : BaseAllatRaFragment(), IManageBackNavFragment {

    private lateinit var passwordView: PasswordView
    private lateinit var passwordConfirmBtn: Button
    private lateinit var backupDesc: TextView
    private lateinit var pswdDesc: TextView
    private lateinit var backupDescExpdCollapse: TextView
    private lateinit var intervalSpinner: AppCompatSpinner
    private lateinit var progress: ContentLoadingProgressBar
    private lateinit var progressDesc: TextView
    private lateinit var progressConfirmation: Button

    private val backupViewModel: BackupViewModel by viewModel()

    private var root: ConstraintLayout? = null
    private val constraintInit = ConstraintSet()
    private val constraintProgress = ConstraintSet()
    private val constraintNoPermission = ConstraintSet()
    private val constraintPassReq = ConstraintSet()
    private var showingKeyboard = false

    private var constraintsState: BackupConstraintState by Delegates.observable(BackupConstraintState.INIT, {_, old, new ->
        if (old != new) {
            TransitionManager.beginDelayedTransition(root as ViewGroup)
            when (new) {
                BackupConstraintState.PASSWORD -> {
                    constraintPassReq.applyTo(root)
                    showPasswordKeyboard()
                }
                BackupConstraintState.PROCESS_STATE -> {
                    constraintProgress.applyTo(root)
                }
                BackupConstraintState.NO_PERMISSION -> {
                    constraintNoPermission.applyTo(root)
                }
                else -> {
                    constraintInit.applyTo(root)
                }
            }
        }
    })

    private val intervalAdapter by lazy {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.backup_settings,
            R.layout.support_simple_spinner_dropdown_item
        )
    }

    private val backupPath: String by lazy {  "../${DIRECTORY_DOWNLOADS}/${BACKUP_FOLDER_NAME}/" }

    private val inputMethodManager by lazy { context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val drawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(newState: Int) {}

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

        override fun onDrawerClosed(drawerView: View) {
            if (constraintsState == BackupConstraintState.PASSWORD) {
                showPasswordKeyboard()
            }
        }

        override fun onDrawerOpened(drawerView: View) {
            hidePasswordKeyboard()
        }
    }

    private val restoreClickListener = View.OnClickListener {
        constraintsState = BackupConstraintState.PASSWORD
        pswdDesc.text = context?.getString(R.string.pswd_req_for_restore)
        pswdConfirmAction { backupViewModel.intent(Restore(it)) }
    }

    private val resetPwdAndBackupClickListener = View.OnClickListener {
        constraintsState = BackupConstraintState.PASSWORD
        pswdConfirmAction { backupViewModel.intent(ResetPwdAndBackup(it)) }
    }

    private val intervalSelectionListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            backupViewModel.intent(IntervalAction(position))
        }
    }

    override fun getFragmentTag() = BACKUP_FRAGMENT_TAG

    override fun toolbarTitle() = R.string.backup_toolbar_title

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.backup_fragment, container, false)
        root = view?.findViewById(R.id.root)

        pswdDesc = view.findViewById(R.id.pswd_desc)
        backupDesc = view.findViewById<TextView>(R.id.backup_description).apply {
            text = getString(R.string.backup_description, backupPath, backupPath)
        }
        intervalSpinner = view.findViewById(R.id.interval_spinner)
        passwordView = view.findViewById(R.id.pswd)
        passwordConfirmBtn = view.findViewById(R.id.pswd_confirm)

        progressDesc = view.findViewById(R.id.progress_desc)
        progress = view.findViewById(R.id.progress)
        progressConfirmation = view.findViewById(R.id.process_confirm)

        setListeners(view)

        return view
    }

    private fun setListeners(view: View) {
        view.findViewById<CardView>(R.id.restore).setOnClickListener(restoreClickListener)
        view.findViewById<CardView>(R.id.reset_and_backup).setOnClickListener(resetPwdAndBackupClickListener)
        view.findViewById<CardView>(R.id.backup_manual).setOnClickListener {
            backupViewModel.intent(Backup)
        }
        view.findViewById<Button>(R.id.permission_req).setOnClickListener {
            backupViewModel.intent(StoragePermissionRequest)
        }

        passwordView.doAfterTextChanged { passwordConfirmBtn.isEnabled = it?.length == 6 }

        progressConfirmation.setOnClickListener { backupViewModel.intent(FinishProcess) }

        backupDescExpdCollapse = view.findViewById(R.id.exp_collapse_desc)
        backupDescExpdCollapse.setOnClickListener {it as TextView
            if (it.isActivated) {
                it.text = getString(R.string.expand_bckp_desc)
                it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_expand
                    ), null
                )
                backupDesc.maxLines = 6
                it.isActivated = false
            } else {
                it.text = getString(R.string.collapse_bckp_desc)
                backupDesc.maxLines = Int.MAX_VALUE
                it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_collapse),
                    null
                )
                it.isActivated = true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        intervalSpinner.adapter = intervalAdapter
        intervalSpinner.onItemSelectedListener = intervalSelectionListener

        constraintInit.clone(root)
        constraintPassReq.clone(context, R.layout.backup_fragment_pswd)
        constraintNoPermission.clone(context, R.layout.backup_fragment_no_perm)
        constraintProgress.clone(context, R.layout.backup_fragment_progress)

        backupViewModel.bind().observe(viewLifecycleOwner, onChanged = ::renderer)
    }

    override fun onResume() {
        super.onResume()
        if (constraintsState == BackupConstraintState.PASSWORD) {
            showPasswordKeyboard()
        }
        getDrawer(activity)?.addDrawerListener(drawerListener)
    }

    override fun onPause() {
        super.onPause()
        getDrawer(activity)?.removeDrawerListener(drawerListener)
        hidePasswordKeyboard()
    }

    private fun renderer(state: BackupViewState) {
        if (intervalSpinner.selectedItemPosition != state.backupInterval) {
            intervalSpinner.setSelection(state.backupInterval)
        }

        when {
            state.reqStoragePerm -> {
                requestStoragePermission()
            }
            state.noStoragePerm && !state.reqStoragePerm-> {
                constraintsState = BackupConstraintState.NO_PERMISSION
            }
            state.backupPwdReq -> {
                constraintsState = BackupConstraintState.PASSWORD
                pswdConfirmAction { backupViewModel.intent(SetupPwd(it)) }
            }
            state.actionState != null -> {
                constraintsState = BackupConstraintState.PROCESS_STATE
                setupProgressInfo(state.actionState)
            }
            else -> {
                constraintsState = BackupConstraintState.INIT
            }
        }
    }

    override fun showInit(): Boolean {
        return when (constraintsState) {
            BackupConstraintState.PROCESS_STATE -> {
                backupViewModel.intent(FinishProcess)
                true
            }
            BackupConstraintState.PASSWORD -> {
                constraintsState = BackupConstraintState.INIT
                true
            }
            else -> {
                false
            }
        }
    }

    private fun setupProgressInfo(actionState: BackupViewState.ActionState) {
        if (actionState.inProgress) {
            progress.show()
            progressConfirmation.isEnabled = false
        } else if (!actionState.inProgress) {
            progressConfirmation.isEnabled = true
            progress.hide()
        }
        progressDesc.text = actionState.resultMsg
    }

    private fun pswdConfirmAction(action: (key: String) -> Unit) {
        passwordConfirmBtn.setOnClickListener {
            action(passwordView.text.toString())
            hidePasswordKeyboard()
        }
    }

    private fun hidePasswordKeyboard() {
        passwordView.editableText.clear()
        inputMethodManager.hideSoftInputFromWindow(passwordView.windowToken, 0)
        passwordView.clearFocus()
    }

    private fun showPasswordKeyboard() {
        if (!showingKeyboard) {
            showingKeyboard = true
            passwordView.requestFocus()
            Handler().postDelayed({
                passwordView.requestFocus()
                inputMethodManager.showSoftInput(passwordView, 0)
                showingKeyboard = false
            }, 300)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for ((position, permission) in permissions.withIndex()) {
                val grantResult = grantResults[position]
                if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    backupViewModel.intent(
                        StoragePermissionStateUpdate(
                            grantResult == PackageManager.PERMISSION_GRANTED
                        )
                    )
                }
            }
        }
    }

    private fun requestStoragePermission() {
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

    enum class BackupConstraintState {
        INIT,
        PASSWORD,
        PROCESS_STATE,
        NO_PERMISSION
    }

    companion object {
        const val BACKUP_FRAGMENT_TAG = "BACKUP_FRAGMENT_TAG"
        const val PERMISSION_REQUEST_CODE = 749
    }
}