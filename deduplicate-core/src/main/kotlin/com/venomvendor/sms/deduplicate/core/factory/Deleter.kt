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

package com.venomvendor.sms.deduplicate.core.factory

import android.net.Uri

/**
 * Deletes messages from the table
 */
interface Deleter {

    /**
     * Unique key in the table
     */
    val primaryKey: String

    /**
     * Content Uri of the table
     */
    val uri: Uri

    /**
     * Deleted given items in batch
     * @param duplicateIds items to be deleted
     * @param deleteBy number of items to be deleted at once
     */
    suspend fun delete(duplicateIds: Collection<String>, deleteBy: Int): Int
}
