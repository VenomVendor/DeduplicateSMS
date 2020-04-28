/*
 *   Copyright (C) 2020 VenomVendor <info@VenomVendor.com>
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

package com.venomvendor.sms.deduplicate.search.manager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import androidx.core.database.getStringOrNull
import com.venomvendor.sms.deduplicate.core.factory.Logger
import com.venomvendor.sms.deduplicate.core.util.Constants
import com.venomvendor.sms.deduplicate.search.data.Message
import com.venomvendor.sms.deduplicate.search.factory.MessageManager
import org.koin.core.KoinComponent
import org.koin.core.get

class MessageGenerator : MessageManager, KoinComponent {

    private val contentResolver = get<ContentResolver>()

    @SuppressLint("Recycle")
    override suspend fun getMessages(
        uri: Uri,
        projection: Array<String>,
        sortOrder: String
    ): List<Message> {
        val logger = get<Logger>()

        val nullableCursor = contentResolver.query(
            uri,
            projection,
            null,
            null,
            sortOrder
        )

        if (nullableCursor == null) {
            logger.log("Cursor is null")
        }

        val cursor = nullableCursor ?: return emptyList()

        val messages = mutableListOf<Message>()

        logger.log("Cursor count: ${cursor.count}")

        extractMessages(cursor, messages, logger)

        if (cursor.isClosed) {
            logger.log("Weird! Who closed the cursor?")
        } else {
            logger.log("Closing cursor")
            cursor.close()
            logger.log("Closed cursor")
        }

        logger.log("Message count: ${messages.count()}")

        return messages
    }

    private fun extractMessages(
        cursor: Cursor,
        messages: MutableList<Message>,
        logger: Logger
    ) {
        while (cursor.moveToNext()) {
            logger.log("Current index: ${cursor.position}")

            try {
                val message = extractMessage(cursor)
                messages.add(message)
            } catch (ex: Exception) {
                logger.log(ex)
            }
        }
    }

    private fun extractMessage(cursor: Cursor): Message {
        val columnIndexId = cursor.getColumnIndex(Constants._ID)
        val id = if (cursor.getType(columnIndexId) == Cursor.FIELD_TYPE_INTEGER) {
            cursor.getInt(columnIndexId).toString()
        } else {
            cursor.getStringOrNull(columnIndexId) ?: ""
        }
        val message = cursor.getStringOrNull(cursor.getColumnIndex(Constants.BODY)) ?: ""
        val phone = cursor.getStringOrNull(cursor.getColumnIndex(Constants.ADDRESS)) ?: ""
        val date = cursor.getStringOrNull(cursor.getColumnIndex(Constants.DATE)) ?: ""

        return Message(id, message, phone, date)
    }
}
