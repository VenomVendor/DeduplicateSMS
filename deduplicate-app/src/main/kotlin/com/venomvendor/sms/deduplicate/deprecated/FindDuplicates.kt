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

package com.venomvendor.sms.deduplicate.deprecated

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.database.Cursor
import android.os.AsyncTask
import android.util.Log
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.venomvendor.sms.deduplicate.BuildConfig
import com.venomvendor.sms.deduplicate.R
import com.venomvendor.sms.deduplicate.util.Constants
import java.util.ArrayList
import java.util.Locale

/*
 * Created by VenomVendor on 11/8/15.
 */
class FindDuplicates(
    @field:SuppressLint("StaticFieldLeak") private val mActivity: Activity,
    private val mIgnoreTimestamp: Boolean,
    private val mIgnoreSpace: Boolean,
    private val mKeepFirst: Boolean,
    private val mCountry: String
) : AsyncTask<Void?, Int?, Boolean?>() {
    private val mDuplicateIds = ArrayList<String>()
    private lateinit var mListener: OnDuplicatesFoundListener
    private var mProgressDialog: ProgressDialog? = null
    private val phoneUtil = PhoneNumberUtil.getInstance()

    fun setOnDuplicatesFoundListener(listener: OnDuplicatesFoundListener) {
        mListener = listener
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mProgressDialog = ProgressDialog(mActivity).apply {
            setMessage(mActivity.getString(R.string.reading_msg))
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            show()
        }
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        val sortOrder =
            if (mIgnoreTimestamp) Constants.DATE + (if (mKeepFirst) " ASC" else " DESC") else null
        val projection = arrayOf(
            Constants._ID,
            Constants.ADDRESS,
            Constants.BODY,
            Constants.DATE,
            Constants.DATE_SENT,
            Constants.TYPE
        )
        val cursor = mActivity.contentResolver.query(
            Constants.CONTENT_URI, projection,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            mProgressDialog!!.max = cursor.count
            val hashCodeCache: MutableList<Int> = ArrayList()
            try {
                mDuplicateIds.clear()
                var currentIndex = 0
                while (cursor.moveToNext()) {
                    collectDuplicates(cursor, hashCodeCache, ++currentIndex)
                }
            } finally {
                hashCodeCache.clear()
                cursor.close()
            }
            return true
        }
        return null
    }

    private fun collectDuplicates(
        cursor: Cursor,
        hashCodeCache: MutableList<Int>,
        currentIndex: Int
    ) {
        val __id = cursor.getInt(cursor.getColumnIndex(Constants._ID))
        val _id: String
        _id = if (__id == 0) {
            cursor.getString(cursor.getColumnIndex(Constants._ID))
        } else {
            __id.toString()
        }

        var message = cursor.getString(cursor.getColumnIndex(Constants.BODY))
        message = message?.toLowerCase(Locale.getDefault())?.trim { it <= ' ' } ?: ""

        if (mIgnoreSpace) {
            message = message.replace("\\s|\\n|\\t|\\r".toRegex(), "")
        }

        val uniqueData = StringBuilder(message)
        val phone = cursor.getString(cursor.getColumnIndex(Constants.ADDRESS))

        var formattedNumber = getFormattedNumber(phone)
        if (formattedNumber.isNotEmpty() && !formattedNumber.startsWith("+")) {
            formattedNumber = reFormatPhone(formattedNumber)
        }
        formattedNumber = formattedNumber.replace("\\s|\\n|\\t|\\r".toRegex(), "")

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$formattedNumber => $message")
        }

        uniqueData.append(formattedNumber)
        if (!mIgnoreTimestamp) {
            uniqueData.append(cursor.getString(cursor.getColumnIndex(Constants.DATE)))
        }

        val hashCode = uniqueData.toString().trim { it <= ' ' }.hashCode()
        if (hashCodeCache.contains(hashCode)) {
            mDuplicateIds.add(_id)
        } else {
            hashCodeCache.add(hashCode)
        }

        publishProgress(currentIndex)
    }

    private fun reFormatPhone(formattedNumber: String): String {
        val filteredNum = formattedNumber.replace("^0+".toRegex(), "")
        return getFormattedNumber(filteredNum)
    }

    private fun getFormattedNumber(phone: String?): String {
        if (phone.isNullOrBlank()) {
            return ""
        }
        try {
            val number = phoneUtil.parseAndKeepRawInput(phone, mCountry)
            val isNumberValid = phoneUtil.isValidNumber(number)
            if (isNumberValid) {
                return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            }
        } catch (ignore: NumberParseException) {
            // Do Nothing.
        }
        return phone
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        mProgressDialog!!.progress = values[0]!!
        mProgressDialog!!.secondaryProgress = mDuplicateIds.size
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        mProgressDialog!!.dismiss()
        mProgressDialog = null
        deleteDuplicates()
    }

    private fun deleteDuplicates() {
        mListener.duplicatesFound(mDuplicateIds)
    }

    interface OnDuplicatesFoundListener {
        fun duplicatesFound(duplicateIds: List<String>)
    }

    companion object {
        private const val TAG = "FindDuplicates"
    }
}
