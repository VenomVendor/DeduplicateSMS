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

import com.venomvendor.sms.deduplicate.core.BaseTest
import com.venomvendor.sms.deduplicate.core.di.MessagingType
import com.venomvendor.sms.deduplicate.core.factory.Deleter
import com.venomvendor.sms.deduplicate.core.factory.DeletionManager
import com.venomvendor.sms.deduplicate.core.factory.WhereClause
import com.venomvendor.sms.deduplicate.core.util.Constants
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.qualifier.named
import org.koin.test.get
import kotlin.math.ceil

internal class SmsDeleterTest : BaseTest() {
    private lateinit var deleter: Deleter

    @BeforeEach
    override fun setUp() {
        super.setUp()
        deleter = get(named(MessagingType.SMS))
    }

    @Test
    internal fun `messaging type must be SMS type`() {
        assertEquals(MessagingType.SMS, deleter.messagingType)
    }

    @Test
    internal fun `primary key should be _id`() {
        assertEquals(Constants._ID, deleter.primaryKey)
    }

    @Test
    internal fun `empty list should delete zero and invoke none`() {
        runBlocking {
            val deletionManager = get<DeletionManager>()
            val deleted = deleter.delete(emptyList(), 10)
            assertEquals(0, deleted)

            verify { deletionManager wasNot Called }
        }
    }

    @Test
    internal fun `list with N items should delete exactly N items`() {
        runBlocking {
            val total = 101
            val split = 10
            val deletionManager = get<DeletionManager>()

            every { deletionManager.delete(WhereClause(any())) } returns split
            every {
                deletionManager.delete(WhereClause(any()))
            } answers {
                args.first().toString().split(",").count()
            }

            val deleted = deleter.delete((1..total).toList().map { it.toString() }, split)
            val timesCalled = ceil(total.toDouble() / split).toInt()

            assertEquals(total, deleted)
            // assertEquals(timesCalled * split, deleted)

            verify(exactly = timesCalled) {
                deletionManager.delete(WhereClause(any()))
            }
        }
    }
}
