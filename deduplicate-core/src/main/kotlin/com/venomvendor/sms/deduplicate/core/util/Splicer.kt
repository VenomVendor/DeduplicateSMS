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

package com.venomvendor.sms.deduplicate.core.util

import java.util.ArrayList

/**
 * Collection that can return spliced ArrayList
 */
class Splicer<T>(collection: Collection<T>) : ArrayList<T>(collection) {

    /**
     * Splice items from collection.
     * Sublist current collection from given [fromIndex] inclusive, to [toIndex] exclusive.
     * This also deletes the sublisted items from current collection.
     */
    fun splice(fromIndex: Int, toIndex: Int): List<T> {
        // Create a new copy
        val splicedList = Splicer(
            subList(
                fromIndex, toIndex
            )
        )

        // Delete spliced items from collection
        removeRange(fromIndex, splicedList.count())

        // Return spliced
        return splicedList
    }
}
