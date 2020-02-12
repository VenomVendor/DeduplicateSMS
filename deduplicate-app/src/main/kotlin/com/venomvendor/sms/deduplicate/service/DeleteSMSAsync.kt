package com.venomvendor.sms.deduplicate.service

import android.content.ContentResolver
import android.content.Context
import android.os.AsyncTask
import com.venomvendor.sms.deduplicate.data.DuplicateList
import com.venomvendor.sms.deduplicate.util.Constants
import java.util.Collections

class DeleteSMSAsync(
    context: Context,
    duplicateIds: List<String>,
    deleteBy: Int,
    onDeletedListener: OnDeletedListener
) : AsyncTask<Void?, Int?, Int>() {
    private val mDeleteBy: Int = deleteBy
    private val mDuplicateIds: DuplicateList<String> = DuplicateList(duplicateIds)
    private val mOnDeletedListener: OnDeletedListener = onDeletedListener
    private val contentResolver: ContentResolver = context.contentResolver
    private var mCancel = false

    override fun doInBackground(vararg voids: Void?): Int {
        var deletedMsgs = 0
        while (!mDuplicateIds.isEmpty() && !mCancel) {
            deletedMsgs = deleteMessages(contentResolver, deletedMsgs)
            mOnDeletedListener.onResponse(deletedMsgs, false)
        }
        return deletedMsgs
    }

    override fun onPostExecute(deletedMsgs: Int) {
        super.onPostExecute(deletedMsgs)
        mOnDeletedListener.onResponse(deletedMsgs, true)
    }

    private fun deleteMessages(contentResolver: ContentResolver, deletedMsgs: Int): Int {
        var deletedMessages = deletedMsgs
        val toIndex = mDeleteBy.coerceAtMost(mDuplicateIds.size)
        val tempIds = mDuplicateIds.splice(0, toIndex)
        val stringBuilder = StringBuilder()
        for (duplicateId in tempIds) {
            stringBuilder.append(duplicateId).append(", ")
        }
        stringBuilder.append("-1")
        val whereClause = String.format(
            "%s in (%s)",
            Constants._ID,
            stringBuilder.toString()
        )
        deletedMessages += contentResolver.delete(
            Constants.CONTENT_URI,
            whereClause,
            null
        )
        return deletedMessages
    }

    fun doCancel() {
        mCancel = true
    }

    init {
        Collections.sort(mDuplicateIds)
    }
}
