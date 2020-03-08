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

package com.venomvendor.sms.deduplicate.core.internal

import android.content.ContentResolver
import android.net.Uri
import android.provider.Telephony
import com.venomvendor.sms.deduplicate.core.util.Splicer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get

/**
 * Deletes SMS from INBOX
 */
class SmsDeleter : CoreDeleter(), KoinComponent {

    override val primaryKey = Telephony.Sms._ID

    override val uri: Uri = Telephony.Sms.CONTENT_URI

    override suspend fun delete(duplicateIds: Collection<String>, deleteBy: Int): Int {
        if (duplicateIds.isEmpty()) {
            return 0
        }

        // Switch to IO Scope
        return withContext(Dispatchers.IO) {
            val duplicateIdx = Splicer(duplicateIds)
            val contentResolver = get<ContentResolver>()

            // delete messages
            return@withContext deleteMessages(contentResolver, duplicateIdx, deleteBy = deleteBy)
        }
    }
}
