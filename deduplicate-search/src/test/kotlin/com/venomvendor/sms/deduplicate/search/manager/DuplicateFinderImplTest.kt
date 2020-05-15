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

import com.venomvendor.sms.deduplicate.search.BaseTest
import com.venomvendor.sms.deduplicate.search.data.Message
import com.venomvendor.sms.deduplicate.search.factory.DuplicateFinder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.get

@ExperimentalCoroutinesApi
internal class DuplicateFinderImplTest : BaseTest(), KoinComponent {

    @Test
    internal fun `duplicate finder is instance of DuplicateFinderImpl`() {
        val duplicateFinder = get<DuplicateFinder>()
        assertTrue(duplicateFinder is DuplicateFinderImpl)
    }

    @Test
    internal fun `empty list of messages produce empty set`() {
        val duplicateFinder = get<DuplicateFinder>()

        val messages = emptyList<Message>()

        runBlockingTest {
            assertTrue(
                duplicateFinder.getDuplicates(
                    messages,
                    "",
                    ignoreTimestamp = false,
                    ignoreSpace = false
                ).isEmpty()
            )
        }
    }

    @Test
    internal fun `list with one item return empty set`() {
        val duplicateFinder = get<DuplicateFinder>()

        val messages = mutableListOf<Message>()

        messages.add(
            Message("1", "1", "+910000000000", "1")
        )

        runBlockingTest {
            assertTrue(
                duplicateFinder.getDuplicates(
                    messages,
                    "",
                    ignoreTimestamp = false,
                    ignoreSpace = false
                ).isEmpty()
            )
        }
    }

    @Test
    internal fun `list with same item return duplicate item`() {
        val duplicateFinder = get<DuplicateFinder>()

        val messages = mutableListOf<Message>()

        messages.add(
            Message("1", "1", "+910000000000", "1")
        )

        messages.add(
            Message("2", "1", "+910000000000", "1")
        )

        runBlockingTest {
            val result = duplicateFinder.getDuplicates(
                messages,
                "",
                ignoreTimestamp = false,
                ignoreSpace = false
            )

            assertEquals(1, result.count())

            assertEquals("2", result.first().id)
        }
    }

    @Test
    internal fun `list with same item with number return duplicate item`() {
        val duplicateFinder = get<DuplicateFinder>()

        val messages = mutableListOf<Message>()

        messages.add(
            Message("1", "1", "+910000000000", "1")
        )

        messages.add(
            Message("2", "1", "+910000000000", "1")
        )

        runBlockingTest {
            val result = duplicateFinder.getDuplicates(
                messages,
                "",
                ignoreTimestamp = false,
                ignoreSpace = false
            )

            assertEquals(1, result.count())

            assertEquals("2", result.first().id)
        }
    }

    @Test
    internal fun `list with same item with space return no duplicate`() {
        val duplicateFinder = get<DuplicateFinder>()

        val messages = mutableListOf<Message>()

        messages.add(
            Message("1", "1", "+910000000000", "1")
        )

        messages.add(
            // Added space around body
            Message("2", "1".padEnd(5).padStart(10), "+910000000000", "1")
        )

        runBlockingTest {
            val result = duplicateFinder.getDuplicates(
                messages,
                "",
                ignoreTimestamp = false,
                ignoreSpace = false
            )

            assertTrue(result.isEmpty())
        }
    }
}
