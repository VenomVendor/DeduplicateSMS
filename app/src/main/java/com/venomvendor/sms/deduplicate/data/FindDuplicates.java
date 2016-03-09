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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;

import com.venomvendor.sms.deduplicate.R;
import com.venomvendor.sms.deduplicate.util.Constants;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by VenomVendor on 11/8/15.
 */
public class FindDuplicates extends AsyncTask<Void, Void, Boolean> {

    private final ArrayList<String> mDuplicateIds = new ArrayList<String>();
    private final List<Integer> mHashCodeCache = new ArrayList<Integer>();
    private final Activity mContext;
    private final boolean mChecked;
    private final boolean mKeepFirst;
    private ProgressDialog mProgressDialog;
    private Cursor mCursor;
    private int mIndex;
    private OnDuplicatesFoundListener mListener;

    public FindDuplicates(Activity activity, boolean checked, boolean keepFirst) {
        this.mContext = activity;
        this.mChecked = checked;
        this.mKeepFirst = keepFirst;
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
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.reading_msg));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String sortOrder = mChecked ? Constants.DATE + (mKeepFirst ? " ASC" : " DESC") : null;
        mCursor = mContext.getContentResolver().query(Constants.CONTENT_URI, null, null, null, sortOrder);
        if (mCursor != null) {
            mProgressDialog.setMax(mCursor.getCount());
            try {
                mDuplicateIds.clear();
                mHashCodeCache.clear();
                while (mCursor.moveToNext()) {
                    mIndex++;
                    collectDuplicates();
                }
            } catch (Exception ignore) {
                return false;
            } finally {
                mHashCodeCache.clear();
                mCursor.close();
            }
            return true;
        }
        return null;
    }

    private void collectDuplicates() {
        final String _id = Integer.toString(mCursor.getInt(0));
        final List<String> uniqueData = new ArrayList<String>();

        uniqueData.add(mCursor.getString(mCursor.getColumnIndex(Constants.ADDRESS)));
        uniqueData.add(mCursor.getString(mCursor.getColumnIndex(Constants.BODY)));
        if (!mChecked) {
            uniqueData.add(mCursor.getString(mCursor.getColumnIndex(Constants.DATE)));
        }
        int hashCode = uniqueData.hashCode();

        if (mHashCodeCache.contains(hashCode)) {
            mDuplicateIds.add(_id);
        } else {
            mHashCodeCache.add(hashCode);
        }
        publishProgress();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setProgress(mIndex);
        mProgressDialog.setSecondaryProgress(mDuplicateIds.size());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mProgressDialog.dismiss();
        showConfirmation();
    }

    private void showConfirmation() {
        if (mDuplicateIds.isEmpty()) {
            deleteDuplicates();
        } else {
            AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(mContext);
            confirmationDialog.setCancelable(false);
            confirmationDialog.setMessage(mContext.getResources()
                    .getQuantityString(R.plurals.delete_duplicates, mDuplicateIds.size(), mDuplicateIds.size()));

            confirmationDialog.setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    deleteDuplicates();
                }
            });
            confirmationDialog.setNegativeButton(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            confirmationDialog.show();
        }
    }

    private void deleteDuplicates() {
        if (mListener == null) {
            throw new NullPointerException("OnDuplicatesFoundListener not implemented.");
        }
        mListener.duplicatesFound(mDuplicateIds);
        mDuplicateIds.clear();
    }

    public interface OnDuplicatesFoundListener {
        void duplicatesFound(ArrayList<String> duplicateIds);
    }

}
