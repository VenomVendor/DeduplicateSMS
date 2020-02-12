/*
 *   Copyright (C) 2015 VenomVendor <info@VenomVendor.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
@file:Suppress("DEPRECATION")

package com.venomvendor.sms.deduplicate.activity

import android.Manifest.permission
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.provider.Telephony.Sms
import android.telephony.TelephonyManager
import android.text.Html
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.venomvendor.sms.deduplicate.BuildConfig
import com.venomvendor.sms.deduplicate.R
import com.venomvendor.sms.deduplicate.data.Country
import com.venomvendor.sms.deduplicate.data.FindDuplicates
import com.venomvendor.sms.deduplicate.data.FindDuplicates.OnDuplicatesFoundListener
import com.venomvendor.sms.deduplicate.service.DeleteSMSAsync
import com.venomvendor.sms.deduplicate.service.OnDeletedListener
import com.venomvendor.sms.deduplicate.util.Constants
import com.venomvendor.sms.deduplicate.util.Utils.isValidMessageApp
import com.venomvendor.sms.deduplicate.util.Utils.revertOldApp
import java.util.ArrayList
import java.util.Locale
import kotlin.math.abs

/*
 * Created by VenomVendor on 11/8/15.
 */
class Deduplication : Activity(), View.OnClickListener {
    private var mTotalMessages = 0

    private lateinit var mFormat: String
    private lateinit var mSmsDeleter: DeleteSMSAsync
    private lateinit var mProgressBarHolder: LinearLayout
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mDeDuplicate: Button
    private lateinit var mCancel: Button
    private lateinit var mRevert: Button
    private lateinit var mDeleted: TextView
    private lateinit var mRevertMessage: TextView
    private lateinit var mDeleteBy: EditText
    private lateinit var mIgnoreTimestamp: CheckedTextView
    private lateinit var mIgnoreSpace: CheckedTextView
    private lateinit var mIgnoreMessage: LinearLayout
    private lateinit var mKeepFirst: RadioButton
    private lateinit var mSpinner: Spinner
    private var mPref: SharedPreferences? = null
    private var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deduplication)
        mPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (mPref!!.getBoolean(Constants.SHOW_EULA, true)) {
            showEula()
        } else {
            initViews()
        }
    }

    private fun showEula() {
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.eula))
            .setCancelable(false)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog: DialogInterface, _: Int ->
                val editor = mPref!!.edit()
                editor.putBoolean(
                    Constants.SHOW_EULA,
                    false
                )
                editor.commit()
                dialog.dismiss()
                initViews()
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    private fun initViews() {
        mPref = null
        mDeDuplicate = findViewById(R.id.deduplicate)
        mCancel = findViewById(R.id.cancel)
        mRevert = findViewById(R.id.revert)
        mDeleted = findViewById(R.id.current_progress)
        mRevertMessage = findViewById(R.id.revert_message)
        mProgressBarHolder = findViewById(R.id.progress_bar_holder)
        mProgressBar = findViewById(R.id.progress_bar)
        mIgnoreTimestamp = findViewById(R.id.ignore_timestamp)
        mIgnoreSpace = findViewById(R.id.ignore_space)
        mIgnoreMessage = findViewById(R.id.ignore_timestamp_message)
        mKeepFirst = findViewById(R.id.keep_first)
        mDeleteBy = findViewById(R.id.per_iteration)
        mSpinner = findViewById(R.id.country_selector)
        addData()
        initListeners()
    }

    private fun addData() {
        val countryCodes = Locale.getISOCountries()
        val countries: MutableList<Country> =
            ArrayList(countryCodes.size + 1)
        for (countryCode in countryCodes) {
            val locale = Locale("", countryCode)
            val countryNameInEng = locale.getDisplayCountry(Locale.ENGLISH)
            val country = Country(locale.country, countryNameInEng)
            countries.add(country)
        }
        countries.sort()
        countries.add(0, Country(country, PRE_SELECT))
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, countries
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = adapter
    }

    private val country: String
        get() {
            val tm =
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.networkCountryIso.toUpperCase(Locale.ENGLISH)
        }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun initListeners() {
        findViewById<View>(R.id.more_apps).setOnClickListener(this)
        mDeDuplicate.setOnClickListener(this)
        mCancel.setOnClickListener(this)
        mRevert.setOnClickListener(this)
        mIgnoreTimestamp.setOnClickListener(this)
        mIgnoreSpace.setOnClickListener(this)
        if (!isValidMessageApp(this)) {
            val pref = PreferenceManager.getDefaultSharedPreferences(
                applicationContext
            )
            val editor = pref.edit()
            editor.putString(
                Constants.CURRENT_SMS_APP,
                Sms.getDefaultSmsPackage(applicationContext)
            )
            editor.apply()
            mRevert.visibility = View.GONE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mRevertMessage.text = Html.fromHtml(getString(R.string.security_reasons))
        } else {
            mRevertMessage.visibility = View.GONE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (checkSelfPermission(REQUIRED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    REQUIRED_PERMISSIONS[0]
                )
            ) { // Educate User
                showCustomDialog(getString(R.string.required_permissions),
                    getString(R.string.m_permission_inform),
                    DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        // ask permissions.
                        requestPermissions(
                            REQUIRED_PERMISSIONS,
                            RUNTIME_PERMISSIONS_CODE
                        )
                    },
                    DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        deadLock()
                    }
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RUNTIME_PERMISSIONS_CODE -> {
                // If request gets cancelled, the result arrays are empty.
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permissionsDenied()
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun permissionsDenied() {
        showCustomDialog(
            getString(R.string.required_permissions),
            String.format(
                getString(R.string.permission_explain),
                getString(R.string.app_name)
            ),
            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                )
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, UPDATE_PERMISSIONS)
            },
            null
        )
    }

    private fun showCustomDialog(
        title: String,
        msg: String,
        listener: DialogInterface.OnClickListener?,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        if (listener == null) {
            throw NullPointerException("listener cannot be null")
        }
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.ok, listener)
        if (cancelListener != null) {
            builder.setNegativeButton(android.R.string.cancel, cancelListener)
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    private fun deadLock() {
        showCustomDialog(
            getString(R.string.permissions_revoked), String.format(
                "%s\n%s %s",
                getString(R.string.app_cannot_run), getString(R.string.app_will_exit),
                getString(android.R.string.ok)
            ),
            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                finish()
            }, null
        )
    }

    private fun showHideWarning() {
        mKeepFirst.isChecked = true
        mIgnoreTimestamp.isChecked = !mIgnoreTimestamp.isChecked
        mIgnoreMessage.visibility = if (mIgnoreTimestamp.isChecked) View.VISIBLE else View.GONE
    }

    private fun doCancel() {
        mSmsDeleter.doCancel()
        mCancel.isEnabled = false
        mCancel.isClickable = false
        mCancel.setText(R.string.cancelling)
        mDeDuplicate.visibility = View.GONE
        mProgressBarHolder.visibility = View.VISIBLE
    }

    private fun cancelDeletion() {
        mCancel.isEnabled = true
        mCancel.isClickable = true
        mCancel.setText(android.R.string.cancel)
        mDeDuplicate.visibility = View.VISIBLE
        mProgressBarHolder.visibility = View.GONE
        revertApp()
    }

    private fun revertApp() {
        Handler().postDelayed({
            revertOldApp(applicationContext)
        }, 1000)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun deduplicate() {
        mProgressBar.max = 0
        mProgressBar.progress = 0
        mDeleted.text = String.format(
            resources
                .getQuantityString(R.plurals.deleted_messages, 0), 0, 0
        )
        mProgressBarHolder.visibility = View.GONE
        if (!isValidMessageApp(this)) {
            val setApp = Intent(Sms.Intents.ACTION_CHANGE_DEFAULT)
            setApp.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivityForResult(setApp, APP_CHANGE_REQUEST)
        } else {
            showWarning()
        }
    }

    private fun showWarning() {
        val warningDialog = AlertDialog.Builder(this)
        warningDialog.setMessage(R.string.warning_message)
        warningDialog.setPositiveButton(
            android.R.string.ok
        ) { _: DialogInterface?, _: Int -> findDuplicates() }
        warningDialog.setNegativeButton(
            android.R.string.cancel
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val dialog = warningDialog.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun findDuplicates() {
        val selectedCountry = (mSpinner.selectedItem as Country).countryCode
        val findDuplicates = FindDuplicates(
            this,
            mIgnoreTimestamp.isChecked,
            mIgnoreSpace.isChecked,
            mKeepFirst.isChecked,
            selectedCountry
        )
        findDuplicates.setOnDuplicatesFoundListener(object : OnDuplicatesFoundListener {
            override fun duplicatesFound(duplicateIds: List<String>) {
                if (duplicateIds.isEmpty()) {
                    showToast(getString(R.string.no_duplicates))
                    revertApp()
                } else {
                    showConfirmation(duplicateIds)
                }
            }
        })
        findDuplicates.execute()
    }

    private fun showConfirmation(duplicateIds: List<String>) {
        mTotalMessages = duplicateIds.size
        mFormat = resources.getQuantityString(R.plurals.deleted_messages, mTotalMessages)
        val confirmationDialog = AlertDialog.Builder(this)
        confirmationDialog.setCancelable(false)
        confirmationDialog.setMessage(
            resources
                .getQuantityString(R.plurals.delete_duplicates, mTotalMessages, mTotalMessages)
        )
        confirmationDialog.setPositiveButton(
            getString(android.R.string.ok)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            mDeDuplicate.visibility = View.GONE
            mProgressBarHolder.visibility = View.VISIBLE
            startDeleteService(duplicateIds)
        }
        confirmationDialog.setNegativeButton(
            getString(android.R.string.cancel)
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        confirmationDialog.show()
    }

    private fun startDeleteService(duplicateIds: List<String>) {
        mProgressBar.max = mTotalMessages
        val deleteBy = perBatch
        mSmsDeleter = DeleteSMSAsync(
            this,
            duplicateIds,
            deleteBy,
            object : OnDeletedListener {
                override fun onResponse(deletedMessages: Int, interrupted: Boolean) {
                    updateDeletedItems(deletedMessages, interrupted)
                }
            }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mSmsDeleter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            mSmsDeleter.execute()
        }
    }

    private val perBatch: Int
        get() {
            var del: String = mDeleteBy.text.toString().trim()
            del = if (del.isBlank()) DEFAULT_DEL.toString() else del
            var deleteBy = abs(del.toInt())
            deleteBy = 1.coerceAtLeast(deleteBy)
            return deleteBy.coerceAtMost(100)
        }

    private fun updateDeletedItems(deletedMessages: Int, interrupted: Boolean) {
        runOnUiThread {
            mDeleted.text = String.format(mFormat, deletedMessages, mTotalMessages)
            if (interrupted) {
                showToast(String.format(mFormat, deletedMessages, mTotalMessages))
                cancelDeletion()
            }
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent
    ) {
        if (requestCode == APP_CHANGE_REQUEST) {
            if (resultCode == RESULT_OK || isValidMessageApp(this)) {
                mRevert.visibility = View.VISIBLE
                mRevertMessage.visibility = View.GONE
                showWarning()
            } else {
                mRevert.visibility = View.GONE
                mRevertMessage.visibility = View.VISIBLE
                showCustomDialog(
                    getString(R.string.failed),
                    getString(R.string.failure_message),
                    DialogInterface.OnClickListener { dialog: DialogInterface, _: Int -> dialog.dismiss() },
                    null
                )
            }
        } else if (requestCode == UPDATE_PERMISSIONS) {
            if (checkSelfPermission(REQUIRED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {
                deadLock()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.deduplicate -> deduplicate()
            R.id.more_apps -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://search?q=pub:VenomVendor")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.cancel -> doCancel()
            R.id.revert -> revertOldApp(
                applicationContext
            )
            R.id.ignore_timestamp -> showHideWarning()
            R.id.ignore_space -> mIgnoreSpace.isChecked = !mIgnoreSpace.isChecked
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mDialog != null) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }

    companion object {
        private const val APP_CHANGE_REQUEST = 253
        private const val UPDATE_PERMISSIONS = 254
        private const val RUNTIME_PERMISSIONS_CODE = 255
        private val REQUIRED_PERMISSIONS = arrayOf(permission.READ_SMS)
        private val DEFAULT_DEL = if (BuildConfig.DEBUG) 2 else 50
        private const val PRE_SELECT = "Select your country"
    }
}
