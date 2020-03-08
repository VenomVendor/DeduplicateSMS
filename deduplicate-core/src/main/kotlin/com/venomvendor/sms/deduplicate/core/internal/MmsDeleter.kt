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

import android.net.Uri
import com.venomvendor.sms.deduplicate.core.factory.Deleter

class MmsDeleter : Deleter {
    override val primaryKey: String
        get() = TODO("Not yet implemented")
    override val uri: Uri
        get() = TODO("Not yet implemented")

    override suspend fun delete(duplicateIds: Collection<String>, deleteBy: Int): Int {
        TODO("Not yet implemented")
    }
}
