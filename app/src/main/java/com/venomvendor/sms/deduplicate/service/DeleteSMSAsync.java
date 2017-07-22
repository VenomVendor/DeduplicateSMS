package com.venomvendor.sms.deduplicate.service;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import com.venomvendor.sms.deduplicate.data.DuplicateList;
import com.venomvendor.sms.deduplicate.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeleteSMSAsync extends AsyncTask<Void, Integer, Integer> {

    private int mDeleteBy;
    private DuplicateList<String> mDuplicateIds;
    private OnDeletedListener mOnDeletedListener;
    private ContentResolver contentResolver;
    private boolean mCancel;

    public DeleteSMSAsync(Context context, ArrayList<String> duplicateIds,
                          int deleteBy, OnDeletedListener onDeletedListener) {
        contentResolver = context.getContentResolver();
        mDuplicateIds = new DuplicateList<>(duplicateIds);
        mDeleteBy = Math.min(deleteBy, 100);
        mOnDeletedListener = onDeletedListener;
        Collections.sort(mDuplicateIds);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int deletedMsgs = 0;

        while (!mDuplicateIds.isEmpty() && !mCancel) {
            deletedMsgs = deleteMessages(contentResolver, deletedMsgs);
            mOnDeletedListener.onResponse(deletedMsgs, false);
        }
        return deletedMsgs;
    }

    @Override
    protected void onPostExecute(Integer deletedMsgs) {
        super.onPostExecute(deletedMsgs);
        mOnDeletedListener.onResponse(deletedMsgs, true);
    }

    private int deleteMessages(ContentResolver contentResolver, int deletedMsgs) {
        int toIndex = Math.min(mDeleteBy, mDuplicateIds.size());
        List<String> tempIds = mDuplicateIds.splice(0, toIndex);

        StringBuilder stringBuilder = new StringBuilder();
        for (String duplicateId : tempIds) {
            stringBuilder.append(duplicateId).append(", ");
        }
        stringBuilder.append("-1");

        String whereClause = String.format("%s in (%s)", Constants._ID, stringBuilder.toString());
        deletedMsgs += contentResolver.delete(Constants.CONTENT_URI, whereClause, null);
        return deletedMsgs;
    }

    public void doCancel() {
        mCancel = true;
    }
}
