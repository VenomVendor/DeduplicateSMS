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

package com.venomvendor.sms.deduplicate.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.venomvendor.sms.deduplicate.BuildConfig;
import com.venomvendor.sms.deduplicate.R;
import com.venomvendor.sms.deduplicate.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Created by VenomVendor on 11/8/15.
 */
public final class FindDuplicates extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "FindDuplicates";
    private final ArrayList<String> mDuplicateIds = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    private final Activity mActivity;
    private final boolean mIgnoreTimestamp;
    private final boolean mIgnoreSpace;
    private final boolean mKeepFirst;
    private final String mCountry;
    private ProgressDialog mProgressDialog;
    private OnDuplicatesFoundListener mListener;
    private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public FindDuplicates(Activity activity, boolean ignoreTimestamp, boolean ignoreSpace,
                          boolean keepFirst, String country) {
        this.mActivity = activity;
        this.mIgnoreTimestamp = ignoreTimestamp;
        this.mIgnoreSpace = ignoreSpace;
        this.mKeepFirst = keepFirst;
        this.mCountry = country;
    }

    public void setOnDuplicatesFoundListener(OnDuplicatesFoundListener listener) {
        if (listener == null) {
            throw new NullPointerException("Listener cannot be null.");
        }
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener == null) {
            throw new NullPointerException("OnDuplicatesFoundListener not implemented.");
        }

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(mActivity.getString(R.string.reading_msg));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String sortOrder = mIgnoreTimestamp ?
                Constants.DATE + (mKeepFirst ? " ASC" : " DESC") :
                null;
        String[] projection = new String[] {
                Constants._ID,
                Constants.ADDRESS,
                Constants.BODY,
                Constants.DATE,
                Constants.DATE_SENT,
                Constants.TYPE
        };
        Cursor cursor = mActivity.getContentResolver().query(Constants.CONTENT_URI, projection,
                null,
                null,
                sortOrder);
        if (cursor != null) {
            mProgressDialog.setMax(cursor.getCount());
            List<Integer> hashCodeCache = new ArrayList<>();
            try {
                mDuplicateIds.clear();
                int currentIndex = 0;
                while (cursor.moveToNext() && getMax()) {
                    currentIndex++;
                    collectDuplicates(cursor, hashCodeCache, currentIndex);
                }
            } catch (Exception ignore) {
                return false;
            } finally {
                hashCodeCache.clear();
                cursor.close();
            }
            return true;
        }
        return null;
    }

    private boolean getMax() {
        return !BuildConfig.DEBUG || mDuplicateIds.size() < 20;
    }

    private void collectDuplicates(Cursor cursor, List<Integer> hashCodeCache, int currentIndex) {
        final int __id = cursor.getInt(cursor.getColumnIndex(Constants._ID));
        final String _id;
        if (__id == 0) {
            _id = cursor.getString(cursor.getColumnIndex(Constants._ID));
        } else {
            _id = String.valueOf(__id);
        }

        String message = cursor.getString(cursor.getColumnIndex(Constants.BODY))
                .toLowerCase(Locale.getDefault())
                .trim();

        if (mIgnoreSpace) {
            message = message.replaceAll("\\s|\\n|\\t|\\r", "");
        }
        StringBuilder uniqueData = new StringBuilder(message);

        String phone = cursor.getString(cursor.getColumnIndex(Constants.ADDRESS));
        String formattedNumber = getFormattedNumber(phone);
        if (!formattedNumber.startsWith("+")) {
            formattedNumber = reFormatPhone(formattedNumber);
        }

        formattedNumber = formattedNumber.replaceAll("\\s|\\n|\\t|\\r", "");

        if (BuildConfig.DEBUG) {
            Log.d(TAG, formattedNumber + " => " + message);
        }

        uniqueData.append(formattedNumber);
        if (!mIgnoreTimestamp) {
            uniqueData.append(cursor.getString(cursor.getColumnIndex(Constants.DATE)));
        }
        int hashCode = uniqueData.toString().trim().hashCode();

        if (hashCodeCache.contains(hashCode)) {
            mDuplicateIds.add(_id);
        } else {
            hashCodeCache.add(hashCode);
        }
        publishProgress(currentIndex);
    }

    private String reFormatPhone(String formattedNumber) {
        String filteredNum = formattedNumber.replaceAll("^0+", "");
        return getFormattedNumber(filteredNum);
    }

    private String getFormattedNumber(String phone) {
        if (phone == null || phone.equals("")) {
            return "";
        }
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parseAndKeepRawInput(phone, mCountry);
            boolean isNumberValid = phoneUtil.isValidNumber(number);
            if (isNumberValid) {
                return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            }
        } catch (NumberParseException ignore) {
            // Do Nothing.
        }
        return phone;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setProgress(values[0]);
        mProgressDialog.setSecondaryProgress(mDuplicateIds.size());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mProgressDialog.dismiss();
        mProgressDialog = null;
        deleteDuplicates();
    }

    private void deleteDuplicates() {
        mListener.duplicatesFound(mDuplicateIds);
    }

    public interface OnDuplicatesFoundListener {

        void duplicatesFound(List<String> duplicateIds);
    }
}
