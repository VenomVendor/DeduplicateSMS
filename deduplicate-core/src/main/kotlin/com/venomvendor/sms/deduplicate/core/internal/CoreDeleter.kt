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
import com.venomvendor.sms.deduplicate.core.factory.Deleter
import com.venomvendor.sms.deduplicate.core.util.Splicer

/**
 * Deletes bulk items from [ContentResolver]
 */
abstract class CoreDeleter : Deleter {

    /**
     * Deletes [splicerIds] from [contentResolver] in [uri]
     * @param splicerIds items to be deleted
     * @param currentDeletedCount count of deleted items
     * @param deleteBy number of items to be deleted at once
     */
    fun deleteMessages(
        contentResolver: ContentResolver,
        splicerIds: Splicer<String>,
        currentDeletedCount: Int = 0,
        deleteBy: Int = 50
    ): Int {
        if (splicerIds.isEmpty()) {
            return currentDeletedCount
        }
        // Get max count to be deleted
        val toIndex = deleteBy.coerceAtMost(splicerIds.count())

        // Extract items to be deleted & removed them from the collection
        val tempIds = splicerIds.splice(0, toIndex).joinToString(",")

        // Create query
        val whereClause = "$primaryKey in ($tempIds)"

        // Updated deleted count
        val deletedMessages = currentDeletedCount.plus(
            contentResolver.delete(uri, whereClause, null)
        )

        // Loop it.
        return deleteMessages(contentResolver, splicerIds, deletedMessages, deleteBy)
    }
}
