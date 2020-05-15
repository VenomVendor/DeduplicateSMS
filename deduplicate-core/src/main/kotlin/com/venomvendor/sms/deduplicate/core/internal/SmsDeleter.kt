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

import com.venomvendor.sms.deduplicate.core.di.MessagingType
import com.venomvendor.sms.deduplicate.core.factory.DeletionManager
import com.venomvendor.sms.deduplicate.core.ktx.DefaultDispatcherProvider
import com.venomvendor.sms.deduplicate.core.ktx.DispatcherProvider
import com.venomvendor.sms.deduplicate.core.util.Constants
import com.venomvendor.sms.deduplicate.core.util.Splicer
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

/**
 * Deletes SMS from INBOX
 */
open class SmsDeleter(private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()) :
    CoreDeleter(), KoinComponent {

    override val primaryKey = Constants._ID

    override val messagingType = MessagingType.SMS

    override suspend fun delete(duplicateIds: Collection<String>, deleteBy: Int): Int {
        if (duplicateIds.isEmpty()) {
            return 0
        }

        // Switch to IO Scope
        return withContext(dispatcher.io()) {
            val duplicateIdx = Splicer(duplicateIds)

            val deletionManager: DeletionManager by inject {
                parametersOf(messagingType)
            }

            // Delete messages
            return@withContext deleteMessages(deletionManager, duplicateIdx, deleteBy = deleteBy)
        }
    }
}
