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
package com.venomvendor.sms.deduplicate.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Telephony.Sms

/*
 * Created by VenomVendor on 11/9/15.
 */
object Utils {
    @JvmStatic
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun revertOldApp(context: Context) {
        if (isValidMessageApp(context)) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val defaultSmsApp = pref.getString(
                Constants.CURRENT_SMS_APP,
                Sms.getDefaultSmsPackage(context)
            )
            val revert = Intent(Sms.Intents.ACTION_CHANGE_DEFAULT)
            revert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            revert.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp)
            context.startActivity(revert)
        }
    }

    @JvmStatic
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun isValidMessageApp(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ||
            context.packageName == Sms.getDefaultSmsPackage(context)
    }
}
