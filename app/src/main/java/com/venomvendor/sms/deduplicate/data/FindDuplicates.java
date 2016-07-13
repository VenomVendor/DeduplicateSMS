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
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;

import com.venomvendor.sms.deduplicate.R;
import com.venomvendor.sms.deduplicate.util.Constants;
import com.venomvendor.sms.deduplicate.util.DiskLogger;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by VenomVendor on 11/8/15.
 */
public class FindDuplicates extends AsyncTask<Void, Void, Boolean> {
    private final ArrayList<String> mDuplicateIds = new ArrayList<String>();
    private final ArrayList<Integer> mIntegerDuplicateIds = new ArrayList<Integer>();
    private final SparseArray<String> mSparseDuplicates = new SparseArray<String>();
    private final List<Integer> mHashCodeCache = new ArrayList<Integer>();
    private final Activity mContext;
    private final boolean mChecked;
    private final boolean mKeepFirst;
    private ProgressDialog mProgressDialog;
    private Cursor mCursor;
    private int mIndex;
    private OnDuplicatesFoundListener mListener;
    private String[] mColumnNames;

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
        DiskLogger.log("FindDuplicates", "onPreExecute()");
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.reading_msg));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DiskLogger.log("FindDuplicates", "doInBackground()");
        String sortOrder = mChecked ? Constants.DATE + (mKeepFirst ? " ASC" : " DESC") : null;

        String[] projection = new String[]{
                Constants._ID,
                Constants.ADDRESS,
                Constants.BODY,
                Constants.DATE,
                Constants.DATE_SENT,
                Constants.TYPE
        };

        mCursor = mContext.getContentResolver().query(Constants.CONTENT_URI, null, null, null, sortOrder);

        if (mCursor == null) {
            return null;
        }
        mColumnNames = mCursor.getColumnNames();
        DiskLogger.log(mColumnNames);

        if (mCursor != null) {
            mProgressDialog.setMax(mCursor.getCount());
            try {
                mIntegerDuplicateIds.clear();
                mDuplicateIds.clear();
                mHashCodeCache.clear();
                while (mCursor.moveToNext()
                        && mHashCodeCache.size() < 1000
                        ) {
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
        try {
            final int _id = mCursor.getInt(mCursor.getColumnIndex(Constants._ID));
            final String _id_ = mCursor.getString(mCursor.getColumnIndex(Constants._ID));
            final List<String> uniqueData = new ArrayList<String>();

            uniqueData.add(mCursor.getString(mCursor.getColumnIndex(Constants.ADDRESS)));
            uniqueData.add(mCursor.getString(mCursor.getColumnIndex(Constants.BODY)));
            if (!mChecked) {
                uniqueData.add(mCursor.getString(mCursor.getColumnIndex(Constants.DATE)));
            }
            int hashCode = uniqueData.hashCode();

            if (mHashCodeCache.contains(hashCode)) {
                ArrayList<String> tempC = new ArrayList<String>(mColumnNames.length);
                for (int i = 0; i < mColumnNames.length; i++) {
                    tempC.add(mCursor.getString(i));
                }
                DiskLogger.log(tempC.toArray(new String[0]));
                mDuplicateIds.add(_id_);
                mIntegerDuplicateIds.add(_id);
                mSparseDuplicates.append(mSparseDuplicates.size(), _id_);
                DiskLogger.log("FindDuplicates", "collectDuplicates STRING ", TextUtils.join(", ", mDuplicateIds));
                DiskLogger.log("FindDuplicates", "collectDuplicates INTEGER", TextUtils.join(", ", mIntegerDuplicateIds));
                try {
                    DiskLogger.log("FindDuplicates", "mSpareDuplicates ", mSparseDuplicates.get(mSparseDuplicates.size() - 1));
                } catch (Exception e) {
                    DiskLogger.log("FindDuplicates", "mSpareDuplicates exception - 1 ", e.getLocalizedMessage());
                }
                tempC.clear();
            } else {
                mHashCodeCache.add(hashCode);
            }
        } catch (Exception e) {
            DiskLogger.log("FindDuplicates", "mSpareDuplicates exception - 2 ", e.getLocalizedMessage());
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
        DiskLogger.log("FindDuplicates", "onPostExecute()");
        mProgressDialog.dismiss();
        showConfirmation();
    }

    private void showConfirmation() {
        DiskLogger.log("FindDuplicates", "showConfirmation()");
        if (mDuplicateIds.isEmpty()) {
            DiskLogger.log("FindDuplicates", "mDuplicateIds.isEmpty()");
            deleteDuplicates();
        } else {
            deleteDuplicates();
//            DiskLogger.log("FindDuplicates", "mDuplicateIds.size(): " + mDuplicateIds.size());
//            AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(mContext);
//            confirmationDialog.setCancelable(false);
//            confirmationDialog.setMessage(mContext.getResources()
//                    .getQuantityString(R.plurals.delete_duplicates, mDuplicateIds.size(), mDuplicateIds.size()));
//
//            confirmationDialog.setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    deleteDuplicates();
//                }
//            });
//            confirmationDialog.setNegativeButton(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            confirmationDialog.show();
        }
    }

    private void deleteDuplicates() {
        DiskLogger.log("FindDuplicates", "deleteDuplicates");
        DiskLogger.log("FindDuplicates", "deleteDuplicates ", TextUtils.join(", ", mDuplicateIds));

        ArrayList<String> clonedDuplicates = new ArrayList<String>(mDuplicateIds);
        DiskLogger.log("FindDuplicates", "clonedDuplicates ", TextUtils.join(", ", clonedDuplicates));

        final int size = mSparseDuplicates.size();
        StringBuilder mStringBuilder = new StringBuilder();
        mStringBuilder.append("mStringBuilder");
        for (int i = 0; i < size; i++) {
            mStringBuilder.append(mSparseDuplicates.get(i)).append(", ");
        }
        DiskLogger.log("FindDuplicates", "SparseDuplicates ", mStringBuilder.toString());

        if (mListener == null) {
            DiskLogger.log("FindDuplicates", "mListener == null");
            throw new NullPointerException("OnDuplicatesFoundListener not implemented.");
        }

        DiskLogger.log("FindDuplicates", "mListener.duplicatesFound(mDuplicateIds)");
        mListener.duplicatesFound(mDuplicateIds);
        mListener.duplicatesFound(mSparseDuplicates);
        // mDuplicateIds.clear();
    }

    public interface OnDuplicatesFoundListener {
        void duplicatesFound(ArrayList<String> duplicateIds);
        void duplicatesFound(SparseArray<String> spareDuplicateIds);
    }
}
